package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBaseBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBaseBlock.class)
public class GSPistonBaseBlockMixin {

	@Shadow @Final private boolean sticky;
	
	@Inject(
		method = "doEvent",
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
	private void onDoEventBlockEntityChanged(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
		if (world.isClient && sticky) {
			// Note: on the client neighbor updates are not executed, which is required
			//       to remove the piston head, but it is already gone on the server at
			//       this point. Remove it here, so it does not remain for another tick.
			Direction direction = state.get(PistonBaseBlock.FACING);
			BlockPos headPos = pos.offset(direction);
			if (world.getBlockState(headPos).getBlock() == Blocks.PISTON_HEAD)
				world.removeBlock(headPos);
		}
	}
}
