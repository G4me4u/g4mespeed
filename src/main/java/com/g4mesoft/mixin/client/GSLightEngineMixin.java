package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

@Mixin(LightEngine.class)
public class GSLightEngineMixin {

	@ModifyReturnValue(
		method = "hasDifferentLightProperties",
		at = @At("RETURN")
	)
	private static boolean onNeedsLightUpdateModifyReturnValue(boolean original, BlockGetter world, BlockPos pos, BlockState oldState, BlockState newState) {
		if (original) {
			// Definitely needs light update.
			return true;
		}
		
		if (oldState.is(Blocks.MOVING_PISTON) || newState.is(Blocks.MOVING_PISTON)) {
			// Annoyingly, since we rely on the block entity for moving blocks, we can
			// not definitively say whether an update is needed. Instead, we must always
			// update regardless of the luminance of the current block entity.
			if (world instanceof Level && ((Level)world).isClientSide) {
				GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
				return tpsModule.cMovingLightSources.get();
			}
		}
		
		return false;
	}
}
