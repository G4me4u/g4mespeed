package com.g4mesoft.mixin.client;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.access.client.GSIWorldRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.packet.GSICustomPayloadPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

/* Use priority -1001 to ensure we have priority over MultiConnect */
@Mixin(value = ClientPlayNetworkHandler.class, priority = -1001)
public class GSClientPlayNetworkHandlerMixin {

	@Shadow @Final private ClientConnection connection;
	@Shadow private MinecraftClient client;
	@Shadow private ClientWorld world;

	private static final int WORLD_TIME_UPDATE_INTERVAL = 20;
	private static final double IGNORE_TELEPORT_MAX_DISTANCE = 1.0; /* Must be > 0.51 */
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		GSClientController.getInstance().setNetworkHandler((ClientPlayNetworkHandler)(Object)this);
	}
	
	@Inject(method = "onGameJoin", at = @At("RETURN"))
	private void onOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		GSClientController.getInstance().onJoinServer();
	}
	
	@Inject(method = "onEntityPosition", cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	private void onOnEntityPosition(EntityPositionS2CPacket packet, CallbackInfo ci) {
		if (GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get()) {
			Entity entity = world.getEntityById(packet.getId());
			if (entity != null && isRecentlyMovedByPiston(entity)) {
				// Update the tracked position such that the entity position
				// does not get out of sync later.
				entity.updateTrackedPosition(packet.getX(), packet.getY(), packet.getZ());
				ci.cancel();
			}
		}
	}
	
	@Inject(method = "onEntityUpdate", cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	private void onOnEntityUpdate(EntityS2CPacket packet, CallbackInfo ci) {
		if (GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get()) {
			Entity entity = packet.getEntity(world);
			if (entity != null && isRecentlyMovedByPiston(entity)) {
				if (!entity.isLogicalSideForUpdatingMovement()) {
					if (packet.isPositionChanged()) {
						// See comment above.
						entity.updateTrackedPosition(packet.calculateDeltaPosition(entity.getTrackedPosition()));
					}
					
					if (packet.hasRotation()) {
						// Do not ignore rotation changes.
						float yaw   = (float)(packet.getYaw()   * 360) / 256.0f;
						float pitch = (float)(packet.getPitch() * 360) / 256.0f;
						entity.updateTrackedPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), yaw, pitch, 3, false);
					}
					
					entity.setOnGround(packet.isOnGround());
				}
				ci.cancel();
			}
		}
	}
	
	@Inject(method = "onPlayerPositionLook", cancellable = true, at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	private void onOnPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
		if (GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get()) {
			// The server will inherently detect that the player moved in an incorrect way, if the
			// player was moved by a piston. In this case we ignore the update and send confirmation.
			// The confirmation is important, since we do not want the server to teleport the player
			// 20 ticks after it has been ignored.
			PlayerEntity player = client.player;
			
			if (isRecentlyMovedByPiston(player)) {
				// Note: there might be a few issues with an actual teleport, if the player was just moved
				//       by a piston. But this should hopefully be solved by a simple distance check.
				boolean isDeltaX = packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.X);
				boolean isDeltaY = packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.Y);
				boolean isDeltaZ = packet.getFlags().contains(PlayerPositionLookS2CPacket.Flag.Z);
				
				double dx = isDeltaX ? packet.getX() : (packet.getX() - player.getX());
				double dy = isDeltaY ? packet.getY() : (packet.getY() - player.getY());
				double dz = isDeltaZ ? packet.getZ() : (packet.getZ() - player.getZ());
				
				if (Math.abs(dx) < IGNORE_TELEPORT_MAX_DISTANCE &&
				    Math.abs(dy) < IGNORE_TELEPORT_MAX_DISTANCE &&
				    Math.abs(dz) < IGNORE_TELEPORT_MAX_DISTANCE) {
					
					connection.send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
					ci.cancel();
				}
			}
		}
	}
	
	@Unique
	private boolean isRecentlyMovedByPiston(Entity entity) {
		return (((GSIEntityAccess)entity).gs_isMovedByPiston() || ((GSIEntityAccess)entity).gs_wasMovedByPiston());
	}
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getPacketManager();
		
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
				if (!tpsModule.sImmediateBlockBroadcast.get() || !tag.contains("ticked") || tag.getBoolean("ticked"))
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
		
		if (tpsModule.sParanoidMode.get()) {
			BlockPos pos = packet.getPos();
			
			if (packet.getBlockEntityType() == 0 && world.isChunkLoaded(pos)) {
				CompoundTag tag = packet.getCompoundTag();

				if ("minecraft:piston".equals(tag.getString("id"))) {
					BlockState blockState = world.getBlockState(pos);
					BlockEntity blockEntity = world.getBlockEntity(pos);
					
					if (!blockState.isOf(Blocks.MOVING_PISTON)) {
						blockState = Blocks.MOVING_PISTON.getDefaultState();
						world.setBlockState(pos, blockState, 4 | 64 /* NO_REDRAW | MOVED */);
					}
					
					// See above redirect method.
					if (!tpsModule.sImmediateBlockBroadcast.get() || !tag.contains("ticked") || tag.getBoolean("ticked"))
						tag.putFloat("progress", Math.min(tag.getFloat("progress") + 0.5f, 1.0f));
					
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

	@Inject(method = "onBlockUpdate", at = @At("RETURN"))
	private void onOnBlockUpdateReturn(BlockUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			scheduleRenderUpdateForFallingBlock(packet.getPos(), packet.getState());
	}
	
	@Inject(method = "onChunkDeltaUpdate", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/network/packet/s2c/play/ChunkDeltaUpdateS2CPacket;visitUpdates(Ljava/util/function/BiConsumer;)V"))
	private void onOnChunkDeltaUpdateReturn(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		if (tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			packet.visitUpdates(this::scheduleRenderUpdateForFallingBlock);
	}
	
	@Unique
	private void scheduleRenderUpdateForFallingBlock(BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof FallingBlock)
			((GSIWorldRendererAccess)client.worldRenderer).gs_scheduleBlockUpdate(pos, true);
	}
}
