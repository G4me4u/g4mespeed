package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(World.class)
public abstract class GSWorldMixin {

	@Shadow @Final public boolean isClient;
	
	@Shadow public abstract BlockState getBlockState(BlockPos pos);
	
	@Unique
	private boolean gs_setBlockState_updatedLight;
	@Unique
	private BlockState gs_setBlockState_oldState;
	
	@Inject(
		method =
			"setBlockState(" +
				"Lnet/minecraft/util/math/BlockPos;" +
				"Lnet/minecraft/block/BlockState;" +
				"I" +
				"I" +
			")Z",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/World;getBlockState(" +
					"Lnet/minecraft/util/math/BlockPos;" +
				")Lnet/minecraft/block/BlockState;"
		)
	)
	private void onSetBlockStateAfterThisGetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir, WorldChunk worldChunk, Block block, BlockState blockState) {
		gs_setBlockState_updatedLight = false;
		gs_setBlockState_oldState = blockState;
	}
	
	@Inject(
		method =
			"setBlockState(" +
				"Lnet/minecraft/util/math/BlockPos;" +
				"Lnet/minecraft/block/BlockState;" +
				"I" +
				"I" +
			")Z",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/chunk/light/LightingProvider;checkBlock(" +
					"Lnet/minecraft/util/math/BlockPos;" +
				")V"
		)
	)
	private void onSetBlockStateAfterLightingProviderCheckBlock(CallbackInfoReturnable<Boolean> cir) {
		gs_setBlockState_updatedLight = true;
	}
	
	@Inject(
		method =
			"setBlockState(" +
				"Lnet/minecraft/util/math/BlockPos;" +
				"Lnet/minecraft/block/BlockState;" +
				"I" +
				"I" +
			")Z",
		at = @At(
			value = "RETURN",
			ordinal = 3
		)
	)
	private void onSetBlockStateReturn3(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
		if (!isClient || gs_setBlockState_updatedLight) {
			// No need to update.
			gs_setBlockState_updatedLight = false;
		} else {
			if (gs_setBlockState_oldState != null) {
				// Annoyingly, since we rely on the block entity for moving blocks, we can
				// not definitively say whether an update is needed. Instead, we must always
				// update regardless of the luminance of the current block entity.
				BlockState newState = this.getBlockState(pos);
				if (gs_setBlockState_oldState.isOf(Blocks.MOVING_PISTON) || newState.isOf(Blocks.MOVING_PISTON)) {
					GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
					if (tpsModule.cMovingLightSources.get())
						((ClientWorld)(Object)this).getChunkManager().getLightingProvider().checkBlock(pos);
				}
				
				gs_setBlockState_oldState = null;
			}
		}
	}
}
