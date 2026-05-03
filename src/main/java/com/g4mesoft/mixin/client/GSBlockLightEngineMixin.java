package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;

@Mixin(BlockLightEngine.class)
public class GSBlockLightEngineMixin {

	@Unique
	private LightChunkGetter gs_chunkProvider;
	
	@Inject(
		method =
			"<init>(" +
				"Lnet/minecraft/world/level/chunk/LightChunkGetter;" +
			")V",
		at = @At("RETURN")
	)
	private void onInit0(LightChunkGetter chunkProvider, CallbackInfo ci) {
		gs_chunkProvider = chunkProvider;
	}
	
	@Inject(
		method =
			"<init>(" +
				"Lnet/minecraft/world/level/chunk/LightChunkGetter;" +
				"Lnet/minecraft/world/level/lighting/BlockLightSectionStorage;" +
			")V",
		at = @At("RETURN")
	)
	private void onInit1(LightChunkGetter chunkProvider, BlockLightSectionStorage blockLightStorage, CallbackInfo ci) {
		gs_chunkProvider = chunkProvider;
	}

	@ModifyExpressionValue(
		method = "getEmission",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(" +
				")I"
		)
	)
	private int onGetLightSourceLuminanceModifyBlockStateGetLuminance(int luminance, long blockPos, BlockState blockState) {
		BlockGetter world = gs_chunkProvider.getLevel();
		if (world instanceof Level && ((Level)world).isClientSide()) {
			BlockPos pos = BlockPos.of(blockPos);
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			return Math.max(luminance, tpsModule.getMovingBlockLuminance(blockState, world, pos));
		}
		return luminance;
	}
	
	@ModifyExpressionValue(
		method = "lambda$propagateLightSources$0",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(" +
				")I"
		)
	)
	private int onPropagateLightSourcesLambdaModifyBlockStateGetLuminance(int luminance, BlockPos pos, BlockState blockState) {
		BlockGetter world = gs_chunkProvider.getLevel();
		if (world instanceof Level && ((Level)world).isClientSide()) {
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			return Math.max(luminance, tpsModule.getMovingBlockLuminance(blockState, world, pos));
		}
		return luminance;
	}
}
