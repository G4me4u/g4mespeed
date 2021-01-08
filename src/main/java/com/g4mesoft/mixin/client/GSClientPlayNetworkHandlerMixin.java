package com.g4mesoft.mixin.client;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSControllerClient;
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
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
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
		GSControllerClient.getInstance().setNetworkHandler((ClientPlayNetworkHandler)(Object) this);
	}
	
	@Inject(method = "onGameJoin", at = @At("RETURN"))
	private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		GSControllerClient.getInstance().onJoinServer();
	}
	
	@Inject(method = "onEntitySpawn", at = @At(value = "INVOKE", shift = Shift.AFTER, 
			target = "Lnet/minecraft/client/world/ClientWorld;addEntity(ILnet/minecraft/entity/Entity;)V"))
	private void onOnEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
		if (packet.getEntityTypeId() == EntityType.FALLING_BLOCK) {
			// Do not rely on locals for the entity, as they might not be
			// consistent with other mods loaded, such as the Fabric API.
			Entity entity = world.getEntityById(packet.getId());
			
			if (entity instanceof FallingBlockEntity) {
				// Falling block entities have an issue where they are ticked
				// one tick later on the client than on the server. This causes
				// the falling blocks to jump at the end of the animation.

				try {
					world.tickEntity(entity);
				} catch (Exception ignore) {
					// If some exception was thrown, ignore it.
				}
			}
		}
	}

	@Inject(method = "onVelocityUpdate", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"))
	private void onOnVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci, Entity entity) {
		if (entity instanceof FallingBlockEntity) {
			// See #onOnEntitySpawn
			
			entity.setVelocity(entity.getVelocity().add(0.0, -0.04, 0.0));
		}
	}

	@Inject(method = "onEntityPosition", locals = LocalCapture.CAPTURE_FAILHARD, at = @At("RETURN"))
	private void onOnEntityPosition(EntityPositionS2CPacket packet, CallbackInfo ci, Entity entity) {
		if (entity instanceof FallingBlockEntity) {
			// See #onOnEntitySpawn

			entity.resetPosition(entity.getX(), entity.getY(), entity.getZ());
			
			entity.move(MovementType.SELF, entity.getVelocity());
			
			// Entity has moved. Make sure its chunk is set correctly.
			world.checkChunk(entity);
		}
	}

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadPacket<ClientPlayPacketListener> payload = (GSICustomPayloadPacket<ClientPlayPacketListener>)packet;
		
		GSControllerClient controllerClient = GSControllerClient.getInstance();
		GSIPacket gsPacket = packetManger.decodePacket(payload, controllerClient.getServerExtensionInfoList(), (ClientPlayNetworkHandler)(Object)this, this.client);
		if (gsPacket != null) {
			gsPacket.handleOnClient(controllerClient);
			ci.cancel();
		}
	}

	@Inject(method = "onWorldTimeUpdate", at = @At("HEAD"))
	private void onWorldTimeSync(WorldTimeUpdateS2CPacket worldTimePacket, CallbackInfo ci) {
		// Handled by GSServerSyncPacket
		GSControllerClient controller = GSControllerClient.getInstance();
		if (controller.isG4mespeedServer())
			return;
		
		if (!this.client.isOnThread())
			controller.getTpsModule().onServerSyncPacket(WORLD_TIME_UPDATE_INTERVAL);
	}
	
	@Redirect(method = "onChunkData", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
	private boolean replaceChunkDataBlockEntityLoop(Iterator<CompoundTag> itr) {
		GSTpsModule tpsModule = GSControllerClient.getInstance().getTpsModule();

		// Note that Fabric Carpet changes parts of the loop, so we have
		// to override the entirety of the look by redirecting the condition.
		
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
				blockEntity.fromTag(tag);
			} else if (pistonType) {
				// Make sure we're actually supposed to put
				// a moving piston block entity in this location...
				BlockState blockState = world.getBlockState(blockPos);
				if (blockState.getBlock() == Blocks.MOVING_PISTON) {
					blockEntity = new PistonBlockEntity();
					blockEntity.fromTag(tag);
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
		GSTpsModule tpsModule = GSControllerClient.getInstance().getTpsModule();
		
		if (tpsModule.sParanoidMode.getValue()) {
			BlockPos pos = packet.getPos();
			
			if (packet.getBlockEntityType() == 0 && world.isChunkLoaded(pos)) {
				BlockState blockState = world.getBlockState(pos);
				
				if (blockState.getBlock() != Blocks.MOVING_PISTON) {
					blockState = Blocks.MOVING_PISTON.getDefaultState();
					world.setBlockStateWithoutNeighborUpdates(pos, blockState);
				}

				BlockEntity blockEntity = world.getBlockEntity(pos);
				CompoundTag tag = packet.getCompoundTag();

				if ("minecraft:piston".equals(tag.getString("id"))) {
					if (!tpsModule.sImmediateBlockBroadcast.getValue() || !tag.contains("ticked") || tag.getBoolean("ticked")) {
						// See above redirect method.
						tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
					}
					
					if (blockEntity == null) {
						blockEntity = new PistonBlockEntity();
						blockEntity.fromTag(tag);
						world.setBlockEntity(pos, blockEntity);
					} else {
						blockEntity.fromTag(tag);
					}

					blockEntity.resetBlock();

					// Cancel vanilla handling of the packet.
					ci.cancel();
				}
			}
		}
	}
	
	@Inject(method = "onBlockUpdate", cancellable = true, at = @At("HEAD"))
	private void onOnBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSControllerClient.getInstance().getTpsModule();

		if (tpsModule.sParanoidMode.getValue() && packet.getState().getBlock() == Blocks.MOVING_PISTON) {
			// In this case we will handle the block state when
			// the block entity has been set in the above injection.
			ci.cancel();
		}
	}
	
	@Inject(method = "onChunkDeltaUpdate", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	private void onOnChunkDeltaUpdateRedirect(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSControllerClient.getInstance().getTpsModule();

		if (tpsModule.sParanoidMode.getValue()) {
			ChunkDeltaUpdateS2CPacket.ChunkDeltaRecord[] records = packet.getRecords();
			
			for (ChunkDeltaUpdateS2CPacket.ChunkDeltaRecord record : records) {
				if (record.getState().getBlock() == Blocks.MOVING_PISTON) {
					BlockState state = world.getBlockState(record.getBlockPos());
					
					if (state.getBlock() != Blocks.MOVING_PISTON) {
						// By setting the block state to the state in the world, it
						// is equivalent to ignoring the block change.
						((GSIChunkDeltaRecordAccess)record).setBlockState(state);
					}
				}
			}
		}
	}
}
