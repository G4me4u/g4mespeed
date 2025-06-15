package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
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
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(World.class)
public abstract class GSWorldMixin {

	@Shadow @Final public boolean isClient;
	
	@Shadow public abstract Profiler getProfiler();
	
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
			value = "INVOKE_ASSIGN",
			target =
				"Lnet/minecraft/world/World;getBlockState(" +
					"Lnet/minecraft/util/math/BlockPos;" +
				")Lnet/minecraft/block/BlockState;"
		)
	)
	private void onSetBlockStateAfterThisGetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir, WorldChunk worldChunk, Block block, BlockState oldState, BlockState newState) {
		if (isClient) {
			// Annoyingly, since we rely on the block entity for moving blocks, we can
			// not definitively say whether an update is needed. Instead, we must always
			// update regardless of the luminance of the current block entity.
			if (oldState.isOf(Blocks.MOVING_PISTON) || newState.isOf(Blocks.MOVING_PISTON)) {
				GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
				if (tpsModule.cMovingLightSources.get()) {
					this.getProfiler().push("queueCheckLight");
					((ClientWorld)(Object)this).getChunkManager().getLightingProvider().checkBlock(pos);
					this.getProfiler().pop();
				}
			}
		}
	}
}
