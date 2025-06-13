package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;

@Mixin(ChunkBlockLightProvider.class)
public class GSChunkBlockLightProviderMixin {

	@Unique
	private ChunkProvider gs_chunkProvider;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(ChunkProvider chunkProvider, CallbackInfo ci) {
		gs_chunkProvider = chunkProvider;
	}
	
	@ModifyExpressionValue(
		method = "getLightSourceLuminance",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/BlockView;getLuminance(" +
					"Lnet/minecraft/util/math/BlockPos;" +
				")I"
		)
	)
	private int onGetLightSourceLuminanceModifyBlockStateGetLuminance(int luminance, long blockPos) {
		BlockView world = gs_chunkProvider.getWorld();
		if (world instanceof World && ((World)world).isClient) {
			BlockPos pos = BlockPos.fromLong(blockPos);
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			return Math.max(luminance, tpsModule.getMovingBlockLuminance(world.getBlockState(pos), world, pos));
		}
		return luminance;
	}
}
