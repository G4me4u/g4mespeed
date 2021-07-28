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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PistonBlockEntity.class)
public class GSPistonBlockEntityMixin extends BlockEntity {

	private boolean ticked;
	
	public GSPistonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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
