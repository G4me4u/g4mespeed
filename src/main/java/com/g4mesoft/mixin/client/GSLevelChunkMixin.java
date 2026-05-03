package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.access.client.GSIPistonMovingBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

@Mixin(LevelChunk.class)
public class GSLevelChunkMixin {
	
	@Shadow @Final private Level level;

	@Unique
	private BlockState gs_setBlockState_oldBlockState;

	@Inject(
		method = "setBlockEntity",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Ljava/util/Map;put(" +
					"Ljava/lang/Object;" +
					"Ljava/lang/Object;" +
				")Ljava/lang/Object;"
		)
	)
	private void onSetBlockEntityAfterPut(BlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity instanceof PistonMovingBlockEntity)
			((GSIPistonMovingBlockEntityAccess)blockEntity).gs_onAdded();
	}
	
	@Inject(
		method = "setBlockState",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE_ASSIGN",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(" +
					"I" +
					"I" +
					"I" +
					"Lnet/minecraft/world/level/block/state/BlockState;" +
				")Lnet/minecraft/world/level/block/state/BlockState;"
		)
	)
	private void onSetBlockAfterChunkSectionSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir, int i, LevelChunkSection chunkSection, boolean bl, int j, int k, int l, BlockState oldBlockState) {
		gs_setBlockState_oldBlockState = oldBlockState;
	}
	
	@ModifyExpressionValue(
		method = "setBlockState",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/level/lighting/LightEngine;hasDifferentLightProperties(" +
					"Lnet/minecraft/world/level/block/state/BlockState;" +
					"Lnet/minecraft/world/level/block/state/BlockState;" +
				")Z"
		)
	)
	private boolean onNeedsLightUpdateModifyReturnValue(boolean original, BlockPos pos, BlockState state, int flags) {
		if (original) {
			// Definitely needs light update.
			return true;
		}
		
		if ((gs_setBlockState_oldBlockState != null && gs_setBlockState_oldBlockState.is(Blocks.MOVING_PISTON)) || state.is(Blocks.MOVING_PISTON)) {
			// Annoyingly, since we rely on the block entity for moving blocks, we can
			// not definitively say whether an update is needed. Instead, we must always
			// update regardless of the luminance of the current block entity.
			if (level.isClientSide()) {
				GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
				return tpsModule.cMovingLightSources.get();
			}
		}
		
		return false;
	}
}
