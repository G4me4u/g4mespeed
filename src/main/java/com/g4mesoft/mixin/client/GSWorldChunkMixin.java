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

import com.g4mesoft.access.client.GSIPistonBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public class GSWorldChunkMixin {
	
	@Shadow @Final private World world;

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
		if (blockEntity instanceof PistonBlockEntity)
			((GSIPistonBlockEntityAccess)blockEntity).gs_onAdded();
	}
	
	@Inject(
		method = "setBlockState",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE_ASSIGN",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/chunk/ChunkSection;setBlockState(" +
					"I" +
					"I" +
					"I" +
					"Lnet/minecraft/block/BlockState;" +
				")Lnet/minecraft/block/BlockState;"
		)
	)
	private void onSetBlockAfterChunkSectionSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir, int i, ChunkSection chunkSection, boolean bl, int j, int k, int l, BlockState oldBlockState) {
		gs_setBlockState_oldBlockState = oldBlockState;
	}
	
	@ModifyExpressionValue(
		method = "setBlockState",
		at = @At(
			value = "INVOKE",
			target =
				"Lnet/minecraft/world/chunk/light/ChunkLightProvider;needsLightUpdate(" +
					"Lnet/minecraft/block/BlockState;" +
					"Lnet/minecraft/block/BlockState;" +
				")Z"
		)
	)
	private boolean onNeedsLightUpdateModifyReturnValue(boolean original, BlockPos pos, BlockState state, int flags) {
		if (original) {
			// Definitely needs light update.
			return true;
		}
		
		if ((gs_setBlockState_oldBlockState != null && gs_setBlockState_oldBlockState.isOf(Blocks.MOVING_PISTON)) || state.isOf(Blocks.MOVING_PISTON)) {
			// Annoyingly, since we rely on the block entity for moving blocks, we can
			// not definitively say whether an update is needed. Instead, we must always
			// update regardless of the luminance of the current block entity.
			if (world.isClient()) {
				GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
				return tpsModule.cMovingLightSources.get();
			}
		}
		
		return false;
	}
}
