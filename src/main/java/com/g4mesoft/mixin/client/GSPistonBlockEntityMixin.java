package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSISmoothPistonBlockEntityAccess;
import com.g4mesoft.core.client.GSControllerClient;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

@Mixin(PistonBlockEntity.class)
public abstract class GSPistonBlockEntityMixin implements GSISmoothPistonBlockEntityAccess {

	private static final float PISTON_STEPS = 3.0f;
	private static final float INCREMENTER = 1.0f / (PISTON_STEPS - 1.0f);
	
	private float actualProgress;
	
	@Shadow private Direction facing;
	
	@Shadow private float nextProgress;
	@Shadow private float progress;

	@Shadow protected abstract float getProgress(float partialTicks);

	@Override
	public float getSmoothProgress(float partialTicks) {
		if (partialTicks > 1.0f)
			partialTicks = 1.0f;
		
		if (G4mespeedMod.getInstance().getSettings().isEnabled()) {
			float val;
			if (GSControllerClient.getInstance().getClientSettings().isSmoothPistonsEnabled()) {
				val = (this.nextProgress * (PISTON_STEPS - 1.0f) + partialTicks) / PISTON_STEPS;
			} else {
				val = actualProgress + (this.nextProgress - actualProgress) * partialTicks;
			}
			
			return Math.min(1.0f, val);
		}
		
		return this.getProgress(partialTicks);
	}

	@Shadow protected abstract float method_11504(float partialTicks);
	
	@Overwrite
	public float getRenderOffsetX(float partialTicks) {
		return (float)this.facing.getOffsetX() * this.method_11504(getSmoothProgress(partialTicks));
	}

	@Overwrite
	public float getRenderOffsetY(float partialTicks) {
		return (float)this.facing.getOffsetY() * this.method_11504(getSmoothProgress(partialTicks));
	}

	@Overwrite
	public float getRenderOffsetZ(float partialTicks) {
		return (float)this.facing.getOffsetZ() * this.method_11504(getSmoothProgress(partialTicks));
	}
	
	@Inject(method = "fromTag", at = @At("RETURN"))
	public void onTagRead(CompoundTag tag, CallbackInfo ci) {
		actualProgress = Math.max(0.0f, this.progress - INCREMENTER);
	}
	
	@Inject(method = "tick", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;progress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onTickProgressChanged(CallbackInfo ci) {
		actualProgress = this.progress;
	}

	@Inject(method = "finish", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;progress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onFinishProgressChanged(CallbackInfo ci) {
		actualProgress = this.progress;
	}
}
