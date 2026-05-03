package com.g4mesoft.mixin.client;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIBlockStatePredictionHandlerAccess;
import com.g4mesoft.access.client.GSIClientLevelAccess;
import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.access.client.GSILevelRendererAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

/* Use priority -1001 to ensure we have priority over MultiConnect */
@Mixin(value = ClientPacketListener.class, priority = -1001)
public abstract class GSClientPacketListenerMixin extends ClientCommonPacketListenerImpl {

	@Shadow @Final private static Logger LOGGER;
	@Shadow private ClientLevel level;

	@Shadow @Final private RegistryAccess.Frozen registryAccess;
	
	protected GSClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}
	
	private static final int WORLD_TIME_UPDATE_INTERVAL = 20;
	private static final double IGNORE_TELEPORT_MAX_DISTANCE = 2.0; /* Must be > 0.51 */

	@Unique
	private GSClientController gs_controller;
	@Unique
	private GSTpsModule gs_tpsModule;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_controller = GSClientController.getInstance();
		gs_tpsModule = gs_controller.getTpsModule();
		
		gs_controller.setNetworkHandler((ClientPacketListener)(Object)this);
	}
	
	@Inject(
		method = "handleLogin",
		at = @At("RETURN")
	)
	private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
		gs_controller.onJoinServer();
	}
	
	@Inject(
		method = "handleEntityPositionSync",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(" +
					"Lnet/minecraft/network/protocol/Packet;" +
					"Lnet/minecraft/network/PacketListener;" +
					"Lnet/minecraft/network/PacketProcessor;" +
				")V"
		)
	)
	private void onHandleEntityPositionSync(ClientboundEntityPositionSyncPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.cCorrectPistonPushing.get()) {
			Entity entity = level.getEntity(packet.id());
			if (entity != null && isRecentlyMovedByPiston(entity)) {
				// Update the tracked position such that the entity position
				// does not get out of sync later.
				Vec3 pos = packet.values().position();
				entity.getPositionCodec().setBase(pos);
				ci.cancel();
			}
		}
	}
	
	@Inject(
		method = "handleMoveEntity",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(" +
					"Lnet/minecraft/network/protocol/Packet;" +
					"Lnet/minecraft/network/PacketListener;" +
					"Lnet/minecraft/network/PacketProcessor;" +
				")V"
		)
	)
	private void onOnEntityUpdate(ClientboundMoveEntityPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.cCorrectPistonPushing.get()) {
			Entity entity = packet.getEntity(level);
			if (entity != null && isRecentlyMovedByPiston(entity)) {
				if (packet.hasPosition()) {
					// See comment above.
					VecDeltaCodec trackedPosition = entity.getPositionCodec();
					Vec3 pos = trackedPosition.decode(packet.getXa(), packet.getYa(), packet.getZa());
					trackedPosition.setBase(pos);
				}
				
				if (!entity.isLocalInstanceAuthoritative()) {
					if (packet.hasRotation()) {
						// Do not ignore rotation changes.
						entity.moveOrInterpolateTo(entity.position(), packet.getYRot(), packet.getXRot());
					}
					
					entity.setOnGround(packet.isOnGround());
				}
				ci.cancel();
			}
		}
	}
	
	@Inject(
		method = "handleMovePlayer",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(" +
					"Lnet/minecraft/network/protocol/Packet;" +
					"Lnet/minecraft/network/PacketListener;" +
					"Lnet/minecraft/network/PacketProcessor;" +
				")V"
		)
	)
	private void onOnPlayerPositionLook(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.cCorrectPistonPushing.get()) {
			// The server will inherently detect that the player moved in an incorrect way, if the
			// player was moved by a piston. In this case we ignore the update and send confirmation.
			// The confirmation is important, since we do not want the server to teleport the player
			// 20 ticks after it has been ignored.
			Player player = minecraft.player;
			
			if (isRecentlyMovedByPiston(player)) {
				// Note: there might be a few issues with an actual teleport, if the player was just moved
				//       by a piston. But this should hopefully be solved by a simple distance check.
				boolean isDeltaX = packet.relatives().contains(Relative.X);
				boolean isDeltaY = packet.relatives().contains(Relative.Y);
				boolean isDeltaZ = packet.relatives().contains(Relative.Z);
				// Note: isDelta* flags specify whether the axes are delta or absolute position.
				Vec3 packetPos = packet.change().position();
				
				double dx = isDeltaX ? packetPos.x : (packetPos.x - player.getX());
				double dy = isDeltaY ? packetPos.y : (packetPos.y - player.getY());
				double dz = isDeltaZ ? packetPos.z : (packetPos.z - player.getZ());
				
				if (Math.abs(dx) < IGNORE_TELEPORT_MAX_DISTANCE &&
				    Math.abs(dy) < IGNORE_TELEPORT_MAX_DISTANCE &&
				    Math.abs(dz) < IGNORE_TELEPORT_MAX_DISTANCE) {
					
					connection.send(new ServerboundAcceptTeleportationPacket(packet.id()));
					ci.cancel();
				}
			}
		}
	}
	
	@Unique
	private boolean isRecentlyMovedByPiston(Entity entity) {
		return (((GSIEntityAccess)entity).gs_isMovedByPiston() || ((GSIEntityAccess)entity).gs_wasMovedByPiston());
	}
	
	@Inject(
		method = "handleSetTime",
		at = @At("HEAD")
	)
	private void onHandleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
		// Check if handled by GSServerSyncPacket (gs server)
		if (!gs_controller.isG4mespeedServer() && !this.minecraft.isSameThread())
			gs_tpsModule.onServerSyncPacket(WORLD_TIME_UPDATE_INTERVAL);
	}
	
	@Inject(
		method = "handleBlockEntityData",
		cancellable = true,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(" +
					"Lnet/minecraft/network/protocol/Packet;" +
					"Lnet/minecraft/network/PacketListener;" +
					"Lnet/minecraft/network/PacketProcessor;" +
				")V"
		)
	)
	private void onOnBlockEntityUpdate(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {
		if (gs_tpsModule.sParanoidMode.get()) {
			BlockPos pos = packet.getPos();
			CompoundTag tag = packet.getTag();
			
			if (!tag.isEmpty() && packet.getType() == BlockEntityType.PISTON) {
				BlockState blockState = level.getBlockState(pos);
				BlockEntity blockEntity = level.getBlockEntity(pos);
				
				if (!blockState.is(Blocks.MOVING_PISTON)) {
					blockState = Blocks.MOVING_PISTON.defaultBlockState();
					// Fix for issue since 1.19.3 where placing a block might reappear
					// due to the sequence being handled later.
					BlockStatePredictionHandler updateManager = ((GSIClientLevelAccess)level).gs_getPendingUpdateManager();
					((GSIBlockStatePredictionHandlerAccess)updateManager).gs_removePendingUpdate(pos);
					level.setServerVerifiedBlockState(pos, blockState, Block.UPDATE_INVISIBLE | Block.UPDATE_MOVE_BY_PISTON);
				}
				
				// Because of a weird issue where the progress saved
				// by a piston is actually 1 gametick old we have to
				// increment the progress by 0.5.
				//
				// Make sure the block entity has actually ticked before
				// we increment the progress. Note that it is guaranteed
				// that the block entity has ticked if it is not a g4mespeed
				// server or if the immediate block updates setting is not
				// enabled.
				if (!gs_tpsModule.sImmediateBlockBroadcast.get() || tag.getBooleanOr("ticked", true))
					tag.putFloat("progress", Math.min(tag.getFloatOr("progress", 0.0f) + 0.5f, 1.0f));
				
				if (blockEntity == null) {
					blockEntity = new PistonMovingBlockEntity(pos, blockState);
					try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
						blockEntity.loadWithComponents(TagValueInput.create(logging, registryAccess, packet.getTag()));
					}
					level.setBlockEntity(blockEntity);
				} else {
					try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
						blockEntity.loadWithComponents(TagValueInput.create(logging, registryAccess, packet.getTag()));
					}
				}

				// Cancel vanilla handling of the packet.
				ci.cancel();
			}
		}
	}

	@Inject(
		method = "handleBlockUpdate",
		at = @At("RETURN")
	)
	private void onHandleBlockUpdateReturn(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
		if (gs_tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			scheduleRenderUpdateForFallingBlock(packet.getPos(), packet.getBlockState());
	}
	
	@Inject(
		method = "handleChunkBlocksUpdate",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/network/protocol/game/ClientboundSectionBlocksUpdatePacket;runUpdates(" +
					"Ljava/util/function/BiConsumer;" +
				")V"
		)
	)
	private void onOnChunkDeltaUpdateReturn(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
		if (gs_tpsModule.sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			packet.runUpdates(this::scheduleRenderUpdateForFallingBlock);
	}
	
	@Unique
	private void scheduleRenderUpdateForFallingBlock(BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof FallingBlock)
			((GSILevelRendererAccess)minecraft.levelRenderer).gs_scheduleBlockUpdate(pos, true);
	}
}
