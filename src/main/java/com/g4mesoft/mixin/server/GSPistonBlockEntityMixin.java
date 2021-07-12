package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.server.GSServerController;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PistonBlockEntity.class)
public class GSPistonBlockEntityMixin extends BlockEntity {

	private boolean ticked;
	
	public GSPistonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	@GSCoreOverride
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		if (GSServerController.getInstance().getTpsModule().sParanoidMode.getValue())
			return new BlockEntityUpdateS2CPacket(pos, 0, toInitialChunkDataNbt());
		return null;
	}
	
	@Override
	@GSCoreOverride
	public NbtCompound toInitialChunkDataNbt() {
		return writeNbt(new NbtCompound());
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
	private void onWriteNbt(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.getTpsModule().sImmediateBlockBroadcast.getValue())
			tag.putBoolean("ticked", ticked);
	}
}
