package com.g4mesoft.mixin.common;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.access.common.GSIServerChunkManagerAccess;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.server.GSServerController;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlock.class)
public class GSPistonBlockMixin {

	@Inject(
		method = "onBlockAction",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/World;setBlockEntity(" +
					"Lnet/minecraft/util/math/BlockPos;" +
					"Lnet/minecraft/block/entity/BlockEntity;" +
				")V"
		)
	)
	private void onOnBlockActionBlockEntityChanged(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
		markBlockEntityForUpdate(world, pos);
	}
	
	@ModifyArg(
		method = "move",
		require = 1,
		index = 2,
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			target =
				"Lnet/minecraft/world/World;setBlockState(" +
					"Lnet/minecraft/util/math/BlockPos;" +
					"Lnet/minecraft/block/BlockState;" +
					"I" +
				")Z"
		)
	)
	private int modifySetAirBlockFlags(int flags) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sParanoidMode.get())
			flags |= 0x02; /* mark block for sending */
		return flags;
	}
	
	@Inject(
		method = "move",
		locals = LocalCapture.CAPTURE_FAILEXCEPTION,
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/World;setBlockEntity(" +
					"Lnet/minecraft/util/math/BlockPos;" +
					"Lnet/minecraft/block/entity/BlockEntity;" +
				")V"
		)
	)
	private void onMoveBlockEntityChanged0(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonHandler pistonHandler, List<?> list, List<?> list2, List<?> list3, int j, BlockState blockStates[], Direction direction, Set<?> set, int k, BlockPos blockPos3) {
		markBlockEntityForUpdate(world, blockPos3);
	}

	@Inject(
		method = "move",
		locals = LocalCapture.CAPTURE_FAILEXCEPTION,
		at = @At(
			value = "INVOKE",
			ordinal = 1,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/World;setBlockEntity(" +
					"Lnet/minecraft/util/math/BlockPos;" +
					"Lnet/minecraft/block/entity/BlockEntity;" +
				")V"
		)
	)
	private void onMoveBlockEntityChanged1(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos) {
		markBlockEntityForUpdate(world, blockPos);
	}
	
	@Unique
	private void markBlockEntityForUpdate(World world, BlockPos pos) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sParanoidMode.get())
			((GSIServerChunkManagerAccess)world.getChunkManager()).gs_markBlockEntityUpdate(pos);
	}
}
