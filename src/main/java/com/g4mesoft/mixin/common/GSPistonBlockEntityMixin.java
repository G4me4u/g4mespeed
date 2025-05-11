package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.GSController;
import com.g4mesoft.core.server.GSServerController;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

@Mixin(PistonBlockEntity.class)
public class GSPistonBlockEntityMixin extends BlockEntity {

	private boolean gs_ticked;
	
	public GSPistonBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		if (GSServerController.getInstance().getTpsModule().sParanoidMode.get())
			return new BlockEntityUpdateS2CPacket(pos, 0, toInitialChunkDataTag());
		return null;
	}
	
	@Override
	public CompoundTag toInitialChunkDataTag() {
		return toTag(new CompoundTag());
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci) {
		gs_ticked = true;
	}
	
	@Inject(
		method = "fromTag",
		at = @At("RETURN")
	)
	private void onFromTag(CompoundTag tag, CallbackInfo ci) {
		gs_ticked = !tag.contains("ticked") || tag.getBoolean("ticked");
	}

	@Inject(
		method = "toTag",
		at = @At("RETURN")
	)
	private void onToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sImmediateBlockBroadcast.get())
			tag.putBoolean("ticked", gs_ticked);
	}
}
