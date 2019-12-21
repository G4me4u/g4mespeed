package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSISmoothPistonBlockEntityAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

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
	
	@Inject(method = "render", at = @At("HEAD"))
	private void onRenderStart(PistonBlockEntity blockEntity, float partialTicks, MatrixStack matrixStack_1, VertexConsumerProvider layeredVertexConsumerStorage_1, int int_1, int int_2, CallbackInfo ci) {
		if (GSControllerClient.getInstance().getTpsModule().cCullMovingBlocks.getValue())
			GlStateManager.enableCull();
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void onRenderEnd(PistonBlockEntity blockEntity, float partialTicks, MatrixStack matrixStack_1, VertexConsumerProvider layeredVertexConsumerStorage_1, int int_1, int int_2, CallbackInfo ci) {
		// Ensure that we're disabling culling after
		// the block entity call (since we might have
		// enabled it).
		GlStateManager.disableCull();
	}
}
