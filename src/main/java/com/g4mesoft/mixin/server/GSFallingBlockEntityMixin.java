package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIServerChunkManagerAccess;
import com.g4mesoft.core.server.GSServerController;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.World;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	public GSFallingBlockEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 0, shift = Shift.AFTER,
			target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	private void onTickRemoveBlock(CallbackInfo ci) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sPrettySand.getValue())
			((GSIServerChunkManagerAccess)world.getChunkManager()).updateBlockImmdiately(getBlockPos());
	}
	
	@Inject(method = "tick", at = @At("RETURN"))
	private void onTickReturn(CallbackInfo ci) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sPrettySand.getValue())
			((GSIServerChunkManagerAccess)world.getChunkManager()).tickEntityTracker(this);
	}

	@Redirect(method = "tick", expect = 1, require = 1, allow = 1, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;sendToOtherNearbyPlayers(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/Packet;)V"))
	private void redirectSendToOtherNearbyPlayers(ThreadedAnvilChunkStorage chunkStorage, Entity entity, Packet<?> packet) {
		if (world.isClient || !GSServerController.getInstance().getTpsModule().sPrettySand.getValue())
			chunkStorage.sendToOtherNearbyPlayers(entity, packet);
	}
}
