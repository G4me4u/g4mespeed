package com.g4mesoft.mixin.client;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSIWorldRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSICustomPayloadPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
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
	private boolean replaceChunkDataBlockEntityLoop(Iterator<CompoundTag> itr) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();

		// Note that Fabric Carpet changes parts of the loop, so we have
		// to override the entirety of the loop by redirecting the condition.
		
		while(itr.hasNext()) {
			CompoundTag tag = itr.next();
			
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
				blockEntity.fromTag(world.getBlockState(blockPos), tag);
			} else if (pistonType) {
				// Make sure we're actually supposed to put
				// a moving piston block entity in this location...
				BlockState blockState = world.getBlockState(blockPos);
				if (blockState.getBlock() == Blocks.MOVING_PISTON) {
					blockEntity = new PistonBlockEntity();
					blockEntity.fromTag(blockState, tag);
					world.setBlockEntity(blockPos, blockEntity);
					
					// Probably not needed but it's done in
					// other places so let's keep the standard.
					blockEntity.resetBlock();
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
					world.setBlockState(pos, blockState, 3 | 8 | 64 /* NOTIFY_ALL | REDRAW_ON_MAIN_THREAD | MOVED */);
				}

				BlockEntity blockEntity = world.getBlockEntity(pos);
				CompoundTag tag = packet.getCompoundTag();

				if ("minecraft:piston".equals(tag.getString("id"))) {
					// The piston block entity has never ticked at the time of receiving the initial
					// placement packet by paranoid mode. See above redirect method.
					//tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
					
					if (blockEntity == null) {
						blockEntity = new PistonBlockEntity();
						blockEntity.fromTag(blockState, tag);
						world.setBlockEntity(pos, blockEntity);
					} else {
						blockEntity.fromTag(blockState, tag);
					}

					blockEntity.resetBlock();

					// Cancel vanilla handling of the packet.
					ci.cancel();
				}
			}
		}
	}

	@Inject(method = "onBlockEvent", cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	private void onOnBlockEvent(BlockEventS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		
		if (tpsModule.sParanoidMode.getValue()) {
			Class<? extends Block> clazz = packet.getBlock().getClass();
			
			if (clazz.equals(PistonBlock.class) || clazz.equals(PistonExtensionBlock.class)) {
				// The block event has already been handled on the server
				// NOTE: Piston extension blocks do not have block events
				//       in vanilla, however, in Redstone Tweaks they do.
				ci.cancel();
			}
		}
	}
	
	@Inject(method = "onBlockUpdate", at = @At("RETURN"))
	private void onOnBlockUpdateReturn(BlockUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED)
			scheduleRenderUpdateForFallingBlock(packet.getPos(), packet.getState());
	}
	
	@Inject(method = "onChunkDeltaUpdate", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/network/packet/s2c/play/ChunkDeltaUpdateS2CPacket;visitUpdates(Ljava/util/function/BiConsumer;)V"))
	private void onOnChunkDeltaUpdateReturn(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED)
			packet.visitUpdates(this::scheduleRenderUpdateForFallingBlock);
	}
	
	private void scheduleRenderUpdateForFallingBlock(BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof FallingBlock)
			((GSIWorldRendererAccess)client.worldRenderer).scheduleBlockUpdate0(pos, true);
	}
}
