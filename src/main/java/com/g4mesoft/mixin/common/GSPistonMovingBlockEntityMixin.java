package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.GSController;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(PistonMovingBlockEntity.class)
public class GSPistonMovingBlockEntityMixin extends BlockEntity {

	@Unique
	private boolean gs_ticked;
	
	public GSPistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private static void onTick(Level world, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
		((GSPistonMovingBlockEntityMixin)(Object)blockEntity).gs_ticked = true;
	}
	
	@Inject(
		method = "loadAdditional",
		at = @At("RETURN")
	)
	private void onLoadAdditional(ValueInput tag, CallbackInfo ci) {
		gs_ticked = tag.getBooleanOr("ticked", true);
	}

	@Inject(
		method = "saveAdditional",
		at = @At("RETURN")
	)
	private void onSaveAdditional(ValueOutput tag, CallbackInfo ci) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sImmediateBlockBroadcast.get())
			tag.putBoolean("ticked", gs_ticked);
	}
}
