package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.access.GSIServerChunkManagerAccess;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;

@Mixin(PistonBlockEntity.class)
public class GSPistonBlockEntityMixin extends BlockEntity {

	private boolean ticked;
	
	public GSPistonBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject(method = "finish", at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private void onFinishReturn(CallbackInfo ci) {
		if (!world.isClient) {
			GSTpsModule tpsModule = GSServerController.getInstance().getTpsModule();
			if (tpsModule.sParanoidMode.getValue() && !tpsModule.sImmediateBlockBroadcast.getValue())
				((GSIServerChunkManagerAccess)world.getChunkManager()).updateBlockImmediately(pos);
		}
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
