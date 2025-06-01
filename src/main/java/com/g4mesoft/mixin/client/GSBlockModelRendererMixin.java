package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

@Mixin(BlockModelRenderer.class)
public class GSBlockModelRendererMixin {

	@ModifyExpressionValue(
		method =
			"render(" +
				"Lnet/minecraft/world/BlockRenderView;" +
				"Lnet/minecraft/client/render/model/BakedModel;" +
				"Lnet/minecraft/block/BlockState;" +
				"Lnet/minecraft/util/math/BlockPos;" +
				"Lnet/minecraft/client/util/math/MatrixStack;" +
				"Lnet/minecraft/client/render/VertexConsumer;" +
				"Z" +
				"Lnet/minecraft/util/math/random/Random;" +
				"J" +
				"I" +
			")V",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/block/BlockState;getLuminance(" +
				")I"
		)
	)
	private int onRenderModifyBlockStateGetLuminance(int luminance, BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		return Math.max(luminance, tpsModule.getMovingBlockLuminance(state, world, pos));
	}
}
