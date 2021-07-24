package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.access.GSIServerChunkManagerAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

@Mixin(FallingBlockEntity.class)
public abstract class GSFallingBlockEntityMixin extends Entity {

	@Shadow public abstract BlockState getBlockState();

	public GSFallingBlockEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 0, shift = Shift.AFTER,
			target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	private void onTickRemoveBlock(CallbackInfo ci) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED)
			((GSIServerChunkManagerAccess)world.getChunkManager()).updateBlockImmdiately(getBlockPos());
	}
	
	@Inject(method = "tick", at = @At(value = "INVOKE", shift = Shift.BEFORE,
			target = "Lnet/minecraft/entity/FallingBlockEntity;remove()V"))
	private void onTickBeforeRemove(CallbackInfo ci) {
		if (!world.isClient && !removed && GSServerController.getInstance().getTpsModule().sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED) {
			((GSIServerChunkManagerAccess)world.getChunkManager()).setTrackerTickedFromFallingBlock(this, true);
			((GSIServerChunkManagerAccess)world.getChunkManager()).tickEntityTracker(this);
		}
	}
	
	@Inject(method = "createSpawnPacket", cancellable = true, at = @At("HEAD"))
	private void onCreateSpawnPacket(CallbackInfoReturnable<Packet<?>> cir) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sPrettySand.getValue() != GSTpsModule.PRETTY_SAND_DISABLED) {
			// Calculate offset applied to position (falling block entity is not 1.0 tall)
			double yOffs = (double)((1.0F - getHeight()) / 2.0F);
			
			cir.setReturnValue(new EntitySpawnS2CPacket(
					getEntityId(), getUuid(),
					getX(), getY() - yOffs, getZ(), pitch, yaw,
					getType(),
					Block.getRawIdFromState(getBlockState()), 
					getVelocity()));
			cir.cancel();
		}
	}
}
