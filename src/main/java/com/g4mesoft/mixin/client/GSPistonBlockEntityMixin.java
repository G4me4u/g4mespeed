package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIPistonBlockEntityAccess;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlockEntity.class)
public abstract class GSPistonBlockEntityMixin extends BlockEntity implements GSIPistonBlockEntityAccess {

	@Shadow private Direction facing;
	
	@Shadow private float progress;
	@Shadow private float lastProgress;
	
	@Shadow private int field_26705;

	private float actualLastProgress;

	/* Number of steps for a full extension (visible / modifiable for mod compatibility) */
	@GSCoreOverride
	private float numberOfSteps = 2.0f;

	public GSPistonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public float getSmoothProgress(float partialTicks) {
		if ((isRemoved() || this.field_26705 != 0) && GSMathUtil.equalsApproximate(this.lastProgress, 1.0f))
			return 1.0f;
		
		float val;
		
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		switch (tpsModule.cPistonAnimationType.getValue()) {
		default:
		case GSTpsModule.PISTON_ANIM_PAUSE_END:
			// Will be clamped by the return statement.
			val = (this.progress * numberOfSteps + partialTicks) / numberOfSteps;
			break;
		case GSTpsModule.PISTON_ANIM_PAUSE_MIDDLE:
			if (this.progress < 0.5f - GSMathUtil.EPSILON_F) {
				val = (this.progress * numberOfSteps + partialTicks) / numberOfSteps;
			} else if (this.progress > 0.5f + GSMathUtil.EPSILON_F) {
				val = (this.progress * numberOfSteps - 1.0f + partialTicks) / numberOfSteps;
			} else {
				val = 0.5f;
			}
			break;
		case GSTpsModule.PISTON_ANIM_PAUSE_BEGINNING:
			val = actualLastProgress + (this.progress - actualLastProgress) * partialTicks;
			break;
		case GSTpsModule.PISTON_ANIM_NO_PAUSE:
			val = (this.progress * numberOfSteps + partialTicks) / (numberOfSteps + 1.0f);
			break;
		}
		
		return Math.min(1.0f, val);
	}

	@Redirect(method = "getRenderOffsetX", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;getProgress(F)F"))
	public float getSmoothRenderOffsetX(PistonBlockEntity blockEntity, float partialTicks) {
		return ((GSIPistonBlockEntityAccess)blockEntity).getSmoothProgress(partialTicks);
	}
	
	@Redirect(method = "getRenderOffsetY", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;getProgress(F)F"))
	public float getSmoothRenderOffsetY(PistonBlockEntity blockEntity, float partialTicks) {
		return ((GSIPistonBlockEntityAccess)blockEntity).getSmoothProgress(partialTicks);
	}
	
	@Redirect(method = "getRenderOffsetZ", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/PistonBlockEntity;getProgress(F)F"))
	public float getSmoothRenderOffsetZ(PistonBlockEntity blockEntity, float partialTicks) {
		return ((GSIPistonBlockEntityAccess)blockEntity).getSmoothProgress(partialTicks);
	}

	@Inject(method = "readNbt", at = @At("RETURN"))
	public void onReadNbt(NbtCompound tag, CallbackInfo ci) {
		actualLastProgress = Math.max(0.0f, this.lastProgress - 1.0f / numberOfSteps);
	}
	
	@Inject(method = "tick", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private static void onTickProgressChanged(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity, CallbackInfo ci) {
		((GSPistonBlockEntityMixin)(Object)blockEntity).actualLastProgress = ((GSPistonBlockEntityMixin)(Object)blockEntity).lastProgress;
	}

	@Inject(method = "finish", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onFinishProgressChanged(CallbackInfo ci) {
		actualLastProgress = this.lastProgress;
	}
}
