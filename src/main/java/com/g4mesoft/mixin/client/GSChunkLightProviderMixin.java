package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.light.ChunkLightProvider;

@Mixin(ChunkLightProvider.class)
public class GSChunkLightProviderMixin {

	@ModifyReturnValue(
		method = "needsLightUpdate",
		at = @At("RETURN")
	)
	private static boolean onNeedsLightUpdateModifyReturnValue(boolean original, BlockView world, BlockPos pos, BlockState oldState, BlockState newState) {
		if (original) {
			// Definitely needs light update.
			return true;
		}
		
		if (oldState.isOf(Blocks.MOVING_PISTON) || newState.isOf(Blocks.MOVING_PISTON)) {
			// Annoyingly, since we rely on the block entity for moving blocks, we can
			// not definitively say whether an update is needed. Instead, we must always
			// update regardless of the luminance of the current block entity.
			if (world instanceof World && ((World)world).isClient) {
				GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
				return tpsModule.cMovingLightSources.get();
			}
		}
		
		return false;
	}
}
