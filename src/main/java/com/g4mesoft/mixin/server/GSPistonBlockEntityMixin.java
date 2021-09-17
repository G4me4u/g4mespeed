package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.GSController;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;

@Mixin(PistonBlockEntity.class)
public class GSPistonBlockEntityMixin extends BlockEntity {

	@Unique
	private boolean ticked;
	
	public GSPistonBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		ticked = true;
	}
	
	@Inject(method = "fromTag", at = @At("RETURN"))
	private void onFromTag(BlockState state, CompoundTag tag, CallbackInfo ci) {
		ticked = !tag.contains("ticked") || tag.getBoolean("ticked");
	}

	@Inject(method = "toTag", at = @At("RETURN"))
	private void onToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sImmediateBlockBroadcast.getValue())
			tag.putBoolean("ticked", ticked);
	}
}
