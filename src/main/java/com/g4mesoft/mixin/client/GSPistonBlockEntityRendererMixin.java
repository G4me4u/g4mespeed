package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.g4mesoft.access.GSISmoothPistonBlockEntityAccess;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;

@Mixin(PistonBlockEntityRenderer.class)
public class GSPistonBlockEntityRendererMixin {
	
	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;getProgress(F)F"))
	public float getPistonProgress(PistonBlockEntity blockEntity, float partialTicks) {
		return ((GSISmoothPistonBlockEntityAccess)blockEntity).getSmoothProgress(partialTicks);
	}
	
	@ModifyConstant(method = "render", constant = @Constant(floatValue = 4.0f))
	private float fixShortArm(float shortArmCutoff) {
		return 0.5f;
	}

	@ModifyConstant(method = "render", constant = @Constant(floatValue = 1.0f), allow = 1)
	private float fixPistonBlink(float maximumProgress) {
		// The progress is fixed in getProgress
		// of the piston block entity.
		return Float.MAX_VALUE;
	}
}
