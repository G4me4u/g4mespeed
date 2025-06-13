package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.BlockLightStorage;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;

@Mixin(ChunkBlockLightProvider.class)
public class GSChunkBlockLightProviderMixin {

	@Unique
	private ChunkProvider gs_chunkProvider;
	
	@Inject(
		method =
			"<init>(" +
				"Lnet/minecraft/world/chunk/ChunkProvider;" +
			")V",
		at = @At("RETURN")
	)
	private void onInit0(ChunkProvider chunkProvider, CallbackInfo ci) {
		gs_chunkProvider = chunkProvider;
	}
	
	@Inject(
		method =
			"<init>(" +
				"Lnet/minecraft/world/chunk/ChunkProvider;" +
				"Lnet/minecraft/world/chunk/light/BlockLightStorage;" +
			")V",
		at = @At("RETURN")
	)
	private void onInit1(ChunkProvider chunkProvider, BlockLightStorage blockLightStorage, CallbackInfo ci) {
		gs_chunkProvider = chunkProvider;
	}

	@ModifyExpressionValue(
		method = "getLightSourceLuminance",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/block/BlockState;getLuminance(" +
				")I"
		)
	)
	private int onGetLightSourceLuminanceModifyBlockStateGetLuminance(int luminance, long blockPos, BlockState blockState) {
		BlockView world = gs_chunkProvider.getWorld();
		if (world instanceof World && ((World)world).isClient) {
			BlockPos pos = BlockPos.fromLong(blockPos);
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			return Math.max(luminance, tpsModule.getMovingBlockLuminance(blockState, world, pos));
		}
		return luminance;
	}
	
	@ModifyExpressionValue(
		method = "method_51532",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/block/BlockState;getLuminance(" +
				")I"
		)
	)
	private int onPropagateLightLambdaModifyBlockStateGetLuminance(int luminance, BlockPos pos, BlockState blockState) {
		BlockView world = gs_chunkProvider.getWorld();
		if (world instanceof World && ((World)world).isClient) {
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			return Math.max(luminance, tpsModule.getMovingBlockLuminance(blockState, world, pos));
		}
		return luminance;
	}
}
