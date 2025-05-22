package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIEntityTrackerAccess;
import com.g4mesoft.access.common.GSIServerChunkMapAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	public GSFallingBlockEntityMixin(World world) {
		super(world);
	}

	@Inject(
		method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/block/state/BlockState;)V",
		at = @At("RETURN")
	)
	private void onInit2(World world, double x, double y, double z, BlockState block, CallbackInfo ci) {
		prevTickX = x;
		prevTickY = y;
		prevTickZ = z;
	}
	
	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/World;removeBlock(" +
					"Lnet/minecraft/util/math/BlockPos;" +
				")Z"
		)
	)
	private void onTickRemoveBlock(CallbackInfo ci) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED)
			((GSIServerChunkMapAccess)((ServerWorld)world).getChunkMap()).gs_updateBlockImmediately(getSourceBlockPos());
	}
	
	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target = "Lnet/minecraft/entity/FallingBlockEntity;remove()V"
		)
	)
	private void onTickBeforeRemove(CallbackInfo ci) {
		if (!world.isClient && !removed && GSServerController.getInstance().getTpsModule().sPrettySand.get() != GSTpsModule.PRETTY_SAND_DISABLED) {
			((GSIEntityTrackerAccess)((ServerWorld)world).getEntityTracker()).gs_setTrackerTickedFromFallingBlock(this, true);
			((GSIEntityTrackerAccess)((ServerWorld)world).getEntityTracker()).gs_tickEntityTracker(this);
		}
	}
}
