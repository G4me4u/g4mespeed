package com.g4mesoft.mixin.client;

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.access.client.GSIMinecraftClientAccess;
import com.g4mesoft.access.client.GSIPistonBlockEntityAccess;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Boxes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

@Mixin(PistonBlockEntity.class)
public abstract class GSPistonBlockEntityMixin extends BlockEntity implements GSIPistonBlockEntityAccess {

	@Shadow private Direction facing;
	
	@Shadow private float progress;
	@Shadow private float lastProgress;
	@Shadow private BlockState pushedBlock;
	
	@Shadow private int field_26705;

	@Shadow protected abstract void pushEntities(float nextProgress);
	
	@Shadow protected abstract void method_23674(float nextProgress);

	@Shadow protected abstract BlockState getHeadBlockState();
	
	@Shadow protected abstract Box offsetHeadBox(Box box);
	
	@Shadow public abstract Direction getMovementDirection();

	private float actualLastProgress;
	
	private float nextProgress = 0.0f;
	private boolean wasAdded = false;
	
	/* Number of steps for a full extension (visible / modifiable for mod compatibility) */
	@GSCoreOverride
	private float numberOfSteps = 2.0f;

	public GSPistonBlockEntityMixin(BlockEntityType<?> blockEntityType_1) {
		super(blockEntityType_1);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float getSmoothProgress(float partialTicks) {
		return getOffsetForProgress(progress, actualLastProgress, partialTicks);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public float getOffsetForProgress(float progress, float lastProgress, float partialTicks) {
		if ((isRemoved() || this.field_26705 != 0) && GSMathUtil.equalsApproximate(lastProgress, 1.0f))
			return 1.0f;
		
		float val;
		
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		switch (tpsModule.cPistonAnimationType.getValue()) {
		default:
		case GSTpsModule.PISTON_ANIM_PAUSE_END:
			// Will be clamped by the return statement.
			val = (progress * numberOfSteps + partialTicks) / numberOfSteps;
			break;
		case GSTpsModule.PISTON_ANIM_PAUSE_MIDDLE:
			if (progress < 0.5f - GSMathUtil.EPSILON_F) {
				val = (progress * numberOfSteps + partialTicks) / numberOfSteps;
			} else if (progress > 0.5f + GSMathUtil.EPSILON_F) {
				val = (progress * numberOfSteps - 1.0f + partialTicks) / numberOfSteps;
			} else {
				val = 0.5f;
			}
			break;
		case GSTpsModule.PISTON_ANIM_PAUSE_BEGINNING:
			val = lastProgress + (progress - lastProgress) * partialTicks;
			break;
		case GSTpsModule.PISTON_ANIM_NO_PAUSE:
			val = (progress * numberOfSteps + partialTicks) / (numberOfSteps + 1.0f);
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
	
	@Inject(method = {"pushEntities", "method_23674"}, at = @At("HEAD"))
	private void onMoveEntitiesHead(float nextProgress, CallbackInfo ci) {
		this.nextProgress = nextProgress;
	}
	
	@ModifyVariable(method = "pushEntities", argsOnly = false, ordinal = 0, at = @At(value = "INVOKE", shift = Shift.BEFORE,
	                target = "Lnet/minecraft/block/entity/PistonBlockEntity;getHeadBlockState()Lnet/minecraft/block/BlockState;"))
	private double onPushEntitiesModifyDeltaProgress(double oldDeltaProgress) {
		return getDeltaProgress(oldDeltaProgress);
	}
	
	@Inject(method = "pushEntities", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/entity/Entity;setVelocity(DDD)V"))
	private void onPushEntitiesAfterEntitySetVelocity(float nextProgress, CallbackInfo ci, Direction direction, double d, VoxelShape voxelShape, Box box, List<?> list, List<?> list2, boolean bl, Iterator<?> var10, Entity entity) {
		((GSIEntityAccess)entity).setMovedByPiston(true);
	}

	@ModifyVariable(method = "method_23674", argsOnly = false, ordinal = 1, at = @At(value = "INVOKE", shift = Shift.BEFORE,
	                target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
	private double onMethod_23674ModifyDeltaProgress(double oldDeltaProgress) {
		return getDeltaProgress(oldDeltaProgress);
	}
	
	private double getDeltaProgress(double oldDeltaProgress) {
		if (shouldCorrectPushEntities()) {
			return ((GSIPistonBlockEntityAccess)this).getOffsetForProgress(nextProgress, progress, 1.0f) -
			       ((GSIPistonBlockEntityAccess)this).getOffsetForProgress(nextProgress, progress, 0.0f);
		}
		return oldDeltaProgress;
	}
	
	@Redirect(method = "offsetHeadBox", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
	          target = "Lnet/minecraft/block/entity/PistonBlockEntity;progress:F"))
	private float onOffsetBoxRedirectProgress(PistonBlockEntity blockEntity) {
		if (shouldCorrectPushEntities())
			return ((GSIPistonBlockEntityAccess)this).getOffsetForProgress(nextProgress, progress, 0.0f);
		return progress;
	}

	@Redirect(method = { "getHeadBlockState", "getCollisionShape" }, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
	          target = "Lnet/minecraft/block/entity/PistonBlockEntity;progress:F"))
	private float onGetCollisionShapeAndHeadStateRedirectProgress(PistonBlockEntity blockEntity) {
		if (shouldCorrectPushEntities())
			return ((GSIPistonBlockEntityAccess)this).getSmoothProgress(1.0f);
		return progress;
	}
	
	@Override
	public void onAdded() {
		if (!wasAdded) {
			if (world.isClient && isPushCorrectionEnabled()) {
				GSClientController controller = GSClientController.getInstance();
				((GSIMinecraftClientAccess)controller.getClient()).schedulePistonBlockEntityUpdate(pos);
			}
			wasAdded = true;
		}
	}
	
	@Override
	public void handleScheduledUpdate() {
		if (isPushCorrectionEnabled()) {
			double deltaProgress = getDeltaProgress(0.0);
			if (GSMathUtil.equalsApproximate(deltaProgress, 0.0)) {
				// pushedBlock = Slime or animation type = Pause at Beginning.
				markEntitiesMovedByPiston(Math.min(1.0f / numberOfSteps, 1.0f));
			} else {
				pushEntities(0.0f);
				method_23674(0.0f);
			}
		}
	}
	
	private void markEntitiesMovedByPiston(float stretchAmount) {
		VoxelShape voxelShape = getHeadBlockState().getCollisionShape(this.world, this.getPos());
		if (!voxelShape.isEmpty()) {
			Box box = offsetHeadBox(voxelShape.getBoundingBox());
			Direction direction = getMovementDirection();
			
			List<Entity> entities = world.getOtherEntities((Entity)null, Boxes.stretch(box, direction, stretchAmount).union(box));
			if (!entities.isEmpty()) {
				Iterator<Entity> entityItr = entities.iterator();

				while (entityItr.hasNext()) {
					Entity entity = entityItr.next();
					if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
						// The player check is not really required, but if we want to
						// run this method on the server, it is probably a good idea.
						if (!(entity instanceof ServerPlayerEntity))
							((GSIEntityAccess)entity).setMovedByPiston(true);
					}
				}
			}
		}
	}
	
	private boolean shouldCorrectPushEntities() {
		return isPushCorrectionEnabled() && !pushedBlock.isOf(Blocks.SLIME_BLOCK);
	}
	
	private boolean isPushCorrectionEnabled() {
		return world.isClient && GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.getValue();
	}

	@Inject(method = "fromTag", at = @At("RETURN"))
	public void onFromTag(BlockState blockState, CompoundTag tag, CallbackInfo ci) {
		actualLastProgress = Math.max(0.0f, this.lastProgress - 1.0f / numberOfSteps);
	}
	
	@Inject(method = {"tick", "finish"}, at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onTickAndFinishProgressChanged(CallbackInfo ci) {
		actualLastProgress = this.lastProgress;
	}

	@Override
	@GSCoreOverride
	@Environment(EnvType.CLIENT)
	public double getSquaredRenderDistance() {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		int chunkDist = tpsModule.cPistonRenderDistance.getValue();
		if (chunkDist == GSTpsModule.AUTOMATIC_PISTON_RENDER_DISTANCE) {
			if (tpsModule.sParanoidMode.getValue()) {
				// When using paranoid mode there is no limit to where
				// the piston block entities might occur. So we just
				// render all of the ones within maximum view distance.
				chunkDist = tpsModule.cPistonRenderDistance.getMaxValue();
			} else {
				chunkDist = tpsModule.sBlockEventDistance.getValue();
			}
		}
		
		return chunkDist * 16.0; // Distance is no longer squared.
	}
}
