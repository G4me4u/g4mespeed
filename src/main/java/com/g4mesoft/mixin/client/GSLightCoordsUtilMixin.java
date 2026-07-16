package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LightCoordsUtil.class)
public class GSLightCoordsUtilMixin {

	@ModifyExpressionValue(
		method =
			"getLightCoords(" +
				"Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;" +
				"Lnet/minecraft/world/level/BlockAndLightGetter;" +
				"Lnet/minecraft/world/level/block/state/BlockState;" +
				"Lnet/minecraft/core/BlockPos;" +
			")I",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(" +
				")I"
		)
	)
	private static int onGetLightColorModifyBlockStateGetLightEmission(int luminance, LightCoordsUtil.BrightnessGetter brightnessGetter, BlockAndLightGetter lightGetter, BlockState state, BlockPos pos) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		return Math.max(luminance, tpsModule.getMovingBlockLuminance(state, lightGetter, pos));
	}
}
