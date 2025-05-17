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
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;

@Mixin(MovingBlockEntity.class)
public class GSMovingBlockEntityMixin extends BlockEntity {

	private boolean gs_ticked;
	
	public GSMovingBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public BlockEntityUpdateS2CPacket createUpdatePacket() {
		if (GSServerController.getInstance().getTpsModule().sParanoidMode.get())
			return new BlockEntityUpdateS2CPacket(pos, 0, toNbt());
		return null;
	}
	
	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci) {
		gs_ticked = true;
	}
	
	@Inject(
		method = "readNbt",
		at = @At("RETURN")
	)
	private void onFromTag(NbtCompound tag, CallbackInfo ci) {
		gs_ticked = !tag.contains("ticked") || tag.getBoolean("ticked");
	}

	@Inject(
		method = "writeNbt",
		at = @At("RETURN")
	)
	private void onToTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sImmediateBlockBroadcast.get())
			tag.putBoolean("ticked", gs_ticked);
	}
}
