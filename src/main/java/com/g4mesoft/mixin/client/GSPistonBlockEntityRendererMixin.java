package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;

@Mixin(PistonBlockEntityRenderer.class)
public class GSPistonBlockEntityRendererMixin {

	@ModifyConstant(
		method = "render",
		constant = @Constant(
			floatValue = 4.0f
		)
	)
	private float fixShortArm(float shortArmCutoff) {
		return 0.5f;
	}

	@ModifyConstant(
		method = "render",
		constant = @Constant(floatValue = 1.0f),
		allow = 1
	)
	private float fixPistonBlink(float maximumProgress) {
		// The progress is fixed in getProgress
		// of the piston block entity.
		return Float.MAX_VALUE;
	}
}
