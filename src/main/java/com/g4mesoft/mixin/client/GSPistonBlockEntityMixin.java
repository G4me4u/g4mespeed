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
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

@Mixin(PistonBlockEntity.class)
public abstract class GSPistonBlockEntityMixin extends BlockEntity implements GSIPistonBlockEntityAccess {

	@Shadow private Direction facing;
	
	@Shadow private float progress;
	@Shadow private float lastProgress;

	private float actualLastProgress;

	/* Number of steps for a full extension (visible / modifiable for mod compatibility) */
	@GSCoreOverride
	private float numberOfSteps = 2.0f;

	public GSPistonBlockEntityMixin(BlockEntityType<?> blockEntityType_1) {
		super(blockEntityType_1);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float getSmoothProgress(float partialTicks) {
		if (isRemoved() && GSMathUtils.equalsApproximate(this.lastProgress, 1.0f))
			return 1.0f;
		
		float val;
		
		GSTpsModule tpsModule = GSControllerClient.getInstance().getTpsModule();
		switch (tpsModule.cPistonAnimationType.getValue()) {
		case GSTpsModule.PISTON_ANIM_NO_PAUSE:
			val = (this.progress * numberOfSteps + partialTicks) / (numberOfSteps + 1.0f);
			break;
		case GSTpsModule.PISTON_ANIM_PAUSE_END:
			// Will be clamped by the return statement.
			val = (this.progress * numberOfSteps + partialTicks) / numberOfSteps;
			break;
		default:
		case GSTpsModule.PISTON_ANIM_PAUSE_BEGINNING:
			val = actualLastProgress + (this.progress - actualLastProgress) * partialTicks;
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

	@Inject(method = "fromTag", at = @At("RETURN"))
	public void onTagRead(CompoundTag tag, CallbackInfo ci) {
		actualLastProgress = Math.max(0.0f, this.lastProgress - 1.0f / numberOfSteps);
	}
	
	@Inject(method = "tick", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onTickProgressChanged(CallbackInfo ci) {
		actualLastProgress = this.lastProgress;
	}

	@Inject(method = "finish", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onFinishProgressChanged(CallbackInfo ci) {
		actualLastProgress = this.lastProgress;
	}
	
	@Override
	@GSCoreOverride
	@Environment(EnvType.CLIENT)
	public double getSquaredRenderDistance() {
		GSTpsModule tpsModule = GSControllerClient.getInstance().getTpsModule();
		int dist = tpsModule.cPistonRenderDistance.getValue();
		if (dist == GSTpsModule.AUTOMATIC_PISTON_RENDER_DISTANCE) {
			if (tpsModule.sParanoidMode.getValue()) {
				// When using paranoid mode there is no limit to where
				// the piston block entities might occur. So we just
				// render all of the ones within maximum view distance.
				dist = tpsModule.cPistonRenderDistance.getMaxValue();
			} else {
				dist = tpsModule.sBlockEventDistance.getValue();
			}
		}
		
		return dist * dist * 256.0; // dist * dist * (16.0 * 16.0)
	}
}
