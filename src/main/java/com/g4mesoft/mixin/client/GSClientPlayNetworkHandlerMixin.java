package com.g4mesoft.mixin.client;

import java.util.Iterator;
import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSICustomPayloadPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayNetworkHandler.class)
public class GSClientPlayNetworkHandlerMixin {

	private static final int WORLD_TIME_UPDATE_INTERVAL = 20;
	
	@Shadow private MinecraftClient client;
	@Shadow private ClientWorld world;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		GSClientController.getInstance().setNetworkHandler((ClientPlayNetworkHandler)(Object)this);
	}
	
	@Inject(method = "onEntitySpawn", at = @At("RETURN"))
	public void onOnEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
		Entity entity = world.getEntityById(packet.getId());
		if (entity != null && entity.getType() == EntityType.FALLING_BLOCK) {
			System.out.println("Packet Spawn: " + packet.getY() + " : " + entity.getY() + " : " + entity.lastRenderY);
		}
	}
	
	@Inject(method = "onEntityPosition", at = @At("RETURN"))
	public void onEntityPosition(EntityPositionS2CPacket packet, CallbackInfo ci) {
		Entity entity = world.getEntityById(packet.getId());
		if (entity != null) {
			System.out.println("Packet Position: " + packet.getY() + " : " + entity.getY() + " : " + entity.lastRenderY);
		}
	}
	
	@Inject(method = "onEntityUpdate", at = @At("RETURN"))
	public void onOnEntityUpdate(EntityS2CPacket packet, CallbackInfo ci) {
		Entity entity = packet.getEntity(world);
		if (entity != null) {
			System.out.println("Packet Update: " + packet.isPositionChanged() + " : " + entity.getY());
		}
	}


	@Inject(method = "onGameJoin", at = @At("RETURN"))
	private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		GSClientController.getInstance().onJoinServer();
	}
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadPacket<ClientPlayPacketListener> payload = (GSICustomPayloadPacket<ClientPlayPacketListener>)packet;
		
		GSClientController controllerClient = GSClientController.getInstance();
		GSIPacket gsPacket = packetManger.decodePacket(payload, controllerClient.getServerExtensionInfoList(), (ClientPlayNetworkHandler)(Object)this, this.client);
		if (gsPacket != null) {
			gsPacket.handleOnClient(controllerClient);
			ci.cancel();
		}
	}

	@Inject(method = "onWorldTimeUpdate", at = @At("HEAD"))
	private void onWorldTimeSync(WorldTimeUpdateS2CPacket worldTimePacket, CallbackInfo ci) {
		// Check if handled by GSServerSyncPacket (gs server)
		GSClientController controller = GSClientController.getInstance();
		if (!controller.isG4mespeedServer() && !this.client.isOnThread())
			controller.getTpsModule().onServerSyncPacket(WORLD_TIME_UPDATE_INTERVAL);
	}
	
	@Redirect(method = "onChunkData", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
	private boolean replaceChunkDataBlockEntityLoop(Iterator<NbtCompound> itr) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();

		// Note that Fabric Carpet changes parts of the loop, so we have
		// to override the entirety of the loop by redirecting the condition.
		
		while(itr.hasNext()) {
			NbtCompound tag = itr.next();
			
			BlockPos blockPos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
			
			boolean pistonType = "minecraft:piston".equals(tag.getString("id"));
			
			if (pistonType) {
				// Because of a weird issue where the progress saved
				// by a piston is actually 1 gametick old we have to
				// increment the progress by 0.5.
				//
				// Make sure the block entity has actually ticked before
				// we increment the progress. Note that it is guaranteed
				// that the block entity has ticked if it is not a g4mespeed
				// server or if the immediate block updates setting is not
				// enabled.
				if (!tpsModule.sImmediateBlockBroadcast.getValue() || !tag.contains("ticked") || tag.getBoolean("ticked"))
					tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
			}
			
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity != null) {
				blockEntity.readNbt(tag);
			} else if (pistonType) {
				// Make sure we're actually supposed to put
				// a moving piston block entity in this location...
				BlockState blockState = world.getBlockState(blockPos);
				if (blockState.getBlock() == Blocks.MOVING_PISTON) {
					blockEntity = new PistonBlockEntity(blockPos, blockState);
					blockEntity.readNbt(tag);
					world.addBlockEntity(blockEntity);
				}
			}
		}
		
		return false;
	}
	
	@Inject(method = "onBlockEntityUpdate", cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER,
		target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	private void onOnBlockEntityUpdate(BlockEntityUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		
		if (tpsModule.sParanoidMode.getValue()) {
			BlockPos pos = packet.getPos();
			
			if (packet.getBlockEntityType() == 0 && world.isChunkLoaded(pos)) {
				BlockState blockState = world.getBlockState(pos);
				
				if (!blockState.isOf(Blocks.MOVING_PISTON)) {
					blockState = Blocks.MOVING_PISTON.getDefaultState();
					world.setBlockStateWithoutNeighborUpdates(pos, blockState);
				}

				BlockEntity blockEntity = world.getBlockEntity(pos);
				NbtCompound tag = packet.getNbt();

				if ("minecraft:piston".equals(tag.getString("id"))) {
					if (!tpsModule.sImmediateBlockBroadcast.getValue() || !tag.contains("ticked") || tag.getBoolean("ticked")) {
						// See above redirect method.
						tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
					}
					
					if (blockEntity == null) {
						blockEntity = new PistonBlockEntity(pos, blockState);
						blockEntity.readNbt(tag);
						world.addBlockEntity(blockEntity);
					} else {
						blockEntity.readNbt(tag);
					}

					// Cancel vanilla handling of the packet.
					ci.cancel();
				}
			}
		}
	}
	
	@Inject(method = "onBlockUpdate", cancellable = true, at = @At("HEAD"))
	private void onOnBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();

		if (tpsModule.sParanoidMode.getValue() && packet.getState().isOf(Blocks.MOVING_PISTON)) {
			// In this case we will handle the block state when
			// the block entity has been set in the above injection.
			ci.cancel();
		}
	}
	
	@ModifyArg(method = "onChunkDeltaUpdate", index = 0, expect = 1, allow = 1, require = 0, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/network/packet/s2c/play/ChunkDeltaUpdateS2CPacket;visitUpdates(Ljava/util/function/BiConsumer;)V"))
	private BiConsumer<BlockPos, BlockState> onOnChunkDeltaUpdateRedirect(BiConsumer<BlockPos, BlockState> handler) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		
		if (tpsModule.sParanoidMode.getValue()) {
			return (pos, state) -> {
				if (!state.isOf(Blocks.MOVING_PISTON))
					handler.accept(pos, state);
			};
		}
		
		return handler;
	}
}
