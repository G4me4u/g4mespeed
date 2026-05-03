package com.g4mesoft.mixin.common;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.access.common.GSIServerChunkCacheAccess;
import com.g4mesoft.core.server.GSServerController;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PistonBaseBlock.class)
public class GSPistonBaseBlockMixin {

	@Inject(
		method = "triggerEvent",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/level/Level;setBlockEntity(" +
					"Lnet/minecraft/world/level/block/entity/BlockEntity;" +
				")V"
		)
	)
	private void onOnSyncedBlockEventBlockEntityChanged(BlockState state, Level world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
		markBlockEntityForUpdate(world, pos);
	}
	
	@Inject(
		method = "moveBlocks",
		locals = LocalCapture.CAPTURE_FAILEXCEPTION,
		at = @At(
			value = "RETURN",
			ordinal = 0,
			shift = Shift.BEFORE
		)
	)
	private void onMoveReturn0(Level world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir) {
		if (!world.isClientSide && GSServerController.getInstance().getTpsModule().sParanoidMode.get())
			((GSIServerChunkCacheAccess)world.getChunkSource()).gs_markBlockUpdate(pos.relative(dir));
	}
	
	@Inject(
		method = "moveBlocks",
		locals = LocalCapture.CAPTURE_FAILEXCEPTION,
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/level/Level;setBlockEntity(" +
					"Lnet/minecraft/world/level/block/entity/BlockEntity;" +
				")V"
		)
	)
	private void onMoveBlockEntityChanged0(Level world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonStructureResolver pistonHandler, Map<?, ?> map, List<?> list, List<?> list2, List<?> list3, BlockState blockStates[], Direction direction, int j, int l, BlockPos blockPos4) {
		markBlockEntityForUpdate(world, blockPos4);
	}

	@Inject(
		method = "moveBlocks",
		locals = LocalCapture.CAPTURE_FAILEXCEPTION,
		at = @At(
			value = "INVOKE",
			ordinal = 1,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/level/Level;setBlockEntity(" +
					"Lnet/minecraft/world/level/block/entity/BlockEntity;" +
				")V"
		)
	)
	private void onMoveBlockEntityChanged1(Level world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos) {
		markBlockEntityForUpdate(world, blockPos);
	}
	
	@Unique
	private void markBlockEntityForUpdate(Level world, BlockPos pos) {
		if (!world.isClientSide && GSServerController.getInstance().getTpsModule().sParanoidMode.get())
			((GSIServerChunkCacheAccess)world.getChunkSource()).gs_markBlockEntityUpdate(pos);
	}
}
