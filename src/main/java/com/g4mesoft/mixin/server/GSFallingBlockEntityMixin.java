package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.server.GSIServerChunkManagerAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	@Shadow public abstract BlockState getBlockState();

	public GSFallingBlockEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", shift = Shift.BEFORE,
			target = "Lnet/minecraft/entity/FallingBlockEntity;discard()V"))
	private void onTickBeforeRemove(CallbackInfo ci) {
		if (!world.isClient && !isRemoved() && GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED) {
			((GSIServerChunkManagerAccess)world.getChunkManager()).gs_setTrackerTickedFromFallingBlock(this, true);
			((GSIServerChunkManagerAccess)world.getChunkManager()).gs_tickEntityTracker(this);
		}
	}
	
	@Redirect(method = "tick", expect = 1, require = 1, allow = 1, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;sendToOtherNearbyPlayers(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/Packet;)V"))
	private void redirectSendToOtherNearbyPlayers(ThreadedAnvilChunkStorage chunkStorage, Entity entity, Packet<?> packet) {
		if (world.isClient || GSServerController.getInstance().getTpsModule().sPrettySand.get() == GSTpsModule.PRETTY_SAND_DISABLED)
			chunkStorage.sendToOtherNearbyPlayers(entity, packet);
	}
	
	@Inject(method = "onSpawnPacket", at = @At(value = "INVOKE", shift = Shift.AFTER,
			target = "Lnet/minecraft/entity/FallingBlockEntity;setFallingBlockPos(Lnet/minecraft/util/math/BlockPos;)V"))
	public void onOnSpawnPacket(EntitySpawnS2CPacket packet, CallbackInfo ci) {
		if (GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			resetPosition();
	}
}
