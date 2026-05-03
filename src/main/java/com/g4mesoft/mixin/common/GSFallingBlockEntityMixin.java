package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIServerChunkCacheAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	@Shadow public abstract BlockState getBlockState();

	public GSFallingBlockEntityMixin(EntityType<?> type, Level world) {
		super(type, world);
	}
	
	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;discard()V"
		)
	)
	private void onTickBeforeRemove(CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide() && !isRemoved() && GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED) {
			((GSIServerChunkCacheAccess)level.getChunkSource()).gs_setTrackerTickedFromFallingBlock(this, true);
			((GSIServerChunkCacheAccess)level.getChunkSource()).gs_tickEntityTracker(this);
		}
	}
	
	@Redirect(
		method = "tick",
		expect = 1,
		require = 1,
		allow = 1,
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/server/level/ChunkMap;sendToTrackingPlayers(" +
					"Lnet/minecraft/world/entity/Entity;" +
					"Lnet/minecraft/network/protocol/Packet;" +
				")V"
		)
	)
	private void redirectSendToOtherNearbyPlayers(ChunkMap chunkStorage, Entity entity, Packet<? super ClientGamePacketListener> packet) {
		Level level = level();
		if (level.isClientSide() || GSServerController.getInstance().getTpsModule().sPrettySand.get() == GSTpsModule.PRETTY_SAND_DISABLED)
			chunkStorage.sendToTrackingPlayers(entity, packet);
	}
	
	@Inject(
		method = "recreateFromPacket",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/entity/item/FallingBlockEntity;setStartPos(" +
					"Lnet/minecraft/core/BlockPos;" +
				")V"
		)
	)
	public void onRecreateFromPacket(ClientboundAddEntityPacket packet, CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			setOldPosAndRot();
	}
}
