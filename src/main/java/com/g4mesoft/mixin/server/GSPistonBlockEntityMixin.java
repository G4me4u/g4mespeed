package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.GSController;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PistonBlockEntity.class)
public class GSPistonBlockEntityMixin extends BlockEntity {

	@Unique
	private boolean ticked;
	
	public GSPistonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private static void onTick(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity, CallbackInfo ci) {
		((GSPistonBlockEntityMixin)(Object)blockEntity).ticked = true;
	}
	
	@Inject(method = "readNbt", at = @At("RETURN"))
	private void onReadNbt(NbtCompound tag, CallbackInfo ci) {
		ticked = !tag.contains("ticked") || tag.getBoolean("ticked");
	}

	@Inject(method = "writeNbt", at = @At("RETURN"))
	private void onWriteNbt(NbtCompound tag, CallbackInfo ci) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sImmediateBlockBroadcast.getValue())
			tag.putBoolean("ticked", ticked);
	}
}
