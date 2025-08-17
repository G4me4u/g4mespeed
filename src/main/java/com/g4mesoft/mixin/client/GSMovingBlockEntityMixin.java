package com.g4mesoft.mixin.client;

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.access.client.GSIMovingBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.ui.util.GSMathUtil;
import com.google.common.collect.Lists;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MovingBlockEntity;
import net.minecraft.block.piston.PistonMoveBehavior;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

@Mixin(MovingBlockEntity.class)
public abstract class GSMovingBlockEntityMixin extends BlockEntity implements GSIMovingBlockEntityAccess {

	@Shadow private Direction facing;
	@Shadow private BlockState movedState;
	@Shadow private boolean extending;
	
	@Shadow private float progress;
	@Shadow private float lastProgress;

	@Shadow public abstract float getProgress(float tickDelta);

	@Shadow protected abstract void moveEntities(float nextProgress);
	
	@Shadow protected abstract BlockState getStateForShape();
	
	@Shadow protected abstract Box getBounds(List<Box> shapes);
	
	@Shadow protected abstract Box moveBox(Box box);

	@Shadow protected abstract Box getMovementArea(Box box, Direction direction, double amount);
	
	private float gs_actualLastProgress;
	@Unique
	private float gs_nextProgress = 0.0f;
	@Unique
	private boolean gs_wasAdded = false;
	
	/* Number of steps for a full extension (visible / modifiable for mod compatibility) */
	private float gs_numberOfSteps = 2.0f;

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onGetProgressHead(float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (world.isClient)
			cir.setReturnValue(gs_getOffsetForProgress(progress, gs_actualLastProgress, tickDelta));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float gs_getOffsetForProgress(float progress, float lastProgress, float tickDelta) {
		if (isRemoved() && GSMathUtil.equalsApproximate(lastProgress, 1.0f))
			return 1.0f;
		
		float val;
		
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		switch (tpsModule.cPistonAnimationType.get()) {
		default:
		case GSTpsModule.PISTON_ANIM_PAUSE_END:
			// Will be clamped by the return statement.
			val = (progress * gs_numberOfSteps + tickDelta) / gs_numberOfSteps;
			break;
		case GSTpsModule.PISTON_ANIM_PAUSE_MIDDLE:
			if (progress < 0.5f - GSMathUtil.EPSILON_F) {
				val = (progress * gs_numberOfSteps + tickDelta) / gs_numberOfSteps;
			} else if (progress > 0.5f + GSMathUtil.EPSILON_F) {
				val = (progress * gs_numberOfSteps - 1.0f + tickDelta) / gs_numberOfSteps;
			} else {
				val = 0.5f;
			}
			break;
		case GSTpsModule.PISTON_ANIM_PAUSE_BEGINNING:
			val = lastProgress + (progress - lastProgress) * tickDelta;
			break;
		case GSTpsModule.PISTON_ANIM_NO_PAUSE:
			val = (progress * gs_numberOfSteps + tickDelta) / (gs_numberOfSteps + 1.0f);
			break;
		}
		
		return Math.min(1.0f, val);
	}

	@Inject(
		method = "moveEntities",
		at = @At("HEAD")
	)
	private void onMoveEntitiesHead(float nextProgress, CallbackInfo ci) {
		this.gs_nextProgress = nextProgress;
	}
	
	@ModifyVariable(
		method = "moveEntities",
		argsOnly = false,
		ordinal = 0,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/block/entity/MovingBlockEntity;getStateForShape(" +
				")Lnet/minecraft/block/state/BlockState;"
		)
	)
	private double onPushEntitiesModifyDeltaProgress(double oldDeltaProgress) {
		return getDeltaProgress(oldDeltaProgress);
	}
	
	@Inject(
		method = "moveEntities",
		locals = LocalCapture.CAPTURE_FAILSOFT,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/util/math/Direction;getAxis(" +
				")Lnet/minecraft/util/math/Direction$Axis;"
		)
	)
	private void onPushEntitiesAfterDirectionGetAxis(float nextProgress, CallbackInfo ci, Direction direction, double d, List<Box> voxelShape, Box box, List<?> list2, boolean bl, int i, Entity entity) {
		((GSIEntityAccess)entity).gs_setMovedByPiston(true);
	}
	
	private double getDeltaProgress(double oldDeltaProgress) {
		if (shouldCorrectPushEntities()) {
			return ((GSIMovingBlockEntityAccess)this).gs_getOffsetForProgress(gs_nextProgress, progress, 1.0f) -
			       ((GSIMovingBlockEntityAccess)this).gs_getOffsetForProgress(gs_nextProgress, progress, 0.0f);
		}
		return oldDeltaProgress;
	}
	
	@Redirect(
		method = "moveBox",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/block/entity/MovingBlockEntity;progress:F"
		)
	)
	private float onOffsetBoxRedirectProgress(MovingBlockEntity blockEntity) {
		if (shouldCorrectPushEntities())
			return ((GSIMovingBlockEntityAccess)this).gs_getOffsetForProgress(gs_nextProgress, progress, 0.0f);
		return progress;
	}

	@Redirect(
		method =
			"getShape(" +
				"Lnet/minecraft/world/WorldView;" +
				"Lnet/minecraft/util/math/BlockPos;" +
			")Lnet/minecraft/util/math/Box;",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/block/entity/MovingBlockEntity;progress:F"
		)
	)
	private float onGetShapeRedirectProgress(MovingBlockEntity blockEntity) {
		if (shouldCorrectPushEntities())
			return getProgress(1.0f);
		return progress;
	}

	@Redirect(
		method =
			"getShape(" +
				"Lnet/minecraft/world/WorldView;" +
				"Lnet/minecraft/util/math/BlockPos;" +
			")Lnet/minecraft/util/math/Box;",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/block/entity/MovingBlockEntity;lastProgress:F"
		)
	)
	private float onGetShapeRedirectLastProgress(MovingBlockEntity blockEntity) {
		if (shouldCorrectPushEntities())
			return getProgress(0.0f);
		return world.isClient ? gs_actualLastProgress : lastProgress;
	}
	
	@Redirect(
		method =
			"addCollisions",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/block/entity/MovingBlockEntity;progress:F"
		)
	)
	private float onAddCollisionsRedirectProgress(MovingBlockEntity blockEntity) {
		if (shouldCorrectPushEntities())
			return getProgress(1.0f);
		return progress;
	}
	
	@Override
	public void gs_onAdded() {
		if (!gs_wasAdded) {
			if (world.isClient && isPushCorrectionEnabled()) {
				GSClientController controller = GSClientController.getInstance();
				((GSIMinecraftAccess)controller.getClient()).gs_schedulePistonBlockEntityUpdate(pos);
			}
			gs_wasAdded = true;
		}
	}
	
	@Override
	public void gs_handleScheduledUpdate() {
		if (isPushCorrectionEnabled()) {
			double deltaProgress = getDeltaProgress(0.0);
			if (GSMathUtil.equalsApproximate(deltaProgress, 0.0)) {
				// pushedBlock = Slime or animation type = Pause at Beginning.
				markEntitiesMovedByPiston(Math.min(1.0f / gs_numberOfSteps, 1.0f));
			} else {
				moveEntities(0.0f);
			}
		}
	}
	
	@Unique
	private void markEntitiesMovedByPiston(float stretchAmount) {
		List<Box> voxelShape = Lists.<Box>newArrayList();
		getStateForShape().addCollisions(world, BlockPos.ORIGIN, new Box(BlockPos.ORIGIN), voxelShape, null, true);
		if (!voxelShape.isEmpty()) {
			Box box = moveBox(getBounds(voxelShape));
			Direction direction = extending ? facing : facing.getOpposite();

			List<Entity> entities = world.getEntities((Entity)null, this.getMovementArea(box, direction, stretchAmount).union(box));
			if (!entities.isEmpty()) {
				Iterator<Entity> entityItr = entities.iterator();

				while (entityItr.hasNext()) {
					Entity entity = entityItr.next();
					if (entity.getPistonMoveBehavior() != PistonMoveBehavior.IGNORE) {
						// The player check is not really required, but if we want to
						// run this method on the server, it is probably a good idea.
						if (!(entity instanceof ServerPlayerEntity))
							((GSIEntityAccess)entity).gs_setMovedByPiston(true);
					}
				}
			}
		}
	}
	
	@Unique
	private boolean shouldCorrectPushEntities() {
		return isPushCorrectionEnabled() && movedState.getBlock() != Blocks.SLIME;
	}
	
	@Unique
	private boolean isPushCorrectionEnabled() {
		return world.isClient && GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get();
	}

	@Inject(
		method = "readNbt",
		at = @At("RETURN")
	)
	private void onReadNbt(NbtCompound tag, CallbackInfo ci) {
		gs_actualLastProgress = Math.max(0.0f, this.lastProgress - 1.0f / gs_numberOfSteps);
	}
	
	@Inject(
		method = { "tick", "finish" },
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/block/entity/MovingBlockEntity;lastProgress:F"
		)
	)
	private void onTickAndFinishProgressChanged(CallbackInfo ci) {
		gs_actualLastProgress = this.lastProgress;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public double getSquaredViewDistance() {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		int dist = tpsModule.cPistonRenderDistance.get();
		if (dist == GSTpsModule.AUTOMATIC_PISTON_RENDER_DISTANCE) {
			if (tpsModule.sParanoidMode.get()) {
				// When using paranoid mode there is no limit to where
				// the piston block entities might occur. So we just
				// render all of the ones within maximum view distance.
				dist = tpsModule.cPistonRenderDistance.getMax();
			} else {
				dist = tpsModule.sBlockEventDistance.get();
			}
		}
		
		return dist * dist * 256.0; // dist * dist * (16.0 * 16.0)
	}
}
