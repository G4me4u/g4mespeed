package com.g4mesoft.mixin.server;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.access.server.GSIServerChunkManagerAccess;
import com.g4mesoft.core.server.GSServerController;

import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlock.class)
public class GSPistonBlockMixin {

	@Inject(method = "onSyncedBlockEvent", at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/world/World;setBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void onOnSyncedBlockEventBlockEntityChanged(BlockState state, World world, BlockPos pos, int type, int data, CallbackInfoReturnable<Boolean> cir) {
		markBlockEntityForUpdate(world, pos);
	}
	
	@Inject(method = "move", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "RETURN", ordinal = 0, shift = Shift.BEFORE))
	private void onMoveReturn0(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sParanoidMode.getValue())
			((GSIServerChunkManagerAccess)world.getChunkManager()).gs_markBlockUpdate(pos.offset(dir));
	}
	
	@Inject(method = "move", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", ordinal = 0, shift = Shift.AFTER,
	        target = "Lnet/minecraft/world/World;setBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void onMoveBlockEntityChanged0(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos, PistonHandler pistonHandler, Map<?, ?> map, List<?> list, List<?> list2, List<?> list3, BlockState blockStates[], Direction direction, int j, int l, BlockPos blockPos4) {
		markBlockEntityForUpdate(world, blockPos4);
	}

	@Inject(method = "move", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", ordinal = 1, shift = Shift.AFTER,
	        target = "Lnet/minecraft/world/World;setBlockEntity(Lnet/	minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)V"))
	private void onMoveBlockEntityChanged1(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> cir, BlockPos blockPos) {
		markBlockEntityForUpdate(world, blockPos);
	}
	
	@Unique
	private void markBlockEntityForUpdate(World world, BlockPos pos) {
		if (!world.isClient && GSServerController.getInstance().getTpsModule().sParanoidMode.getValue())
			((GSIServerChunkManagerAccess)world.getChunkManager()).gs_markBlockEntityUpdate(pos);
	}
}
