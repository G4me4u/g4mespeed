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
import com.g4mesoft.access.client.GSIPistonMovingBlockEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.ui.util.GSMathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMath;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(PistonMovingBlockEntity.class)
public abstract class GSPistonMovingBlockEntityMixin extends BlockEntity implements GSIPistonMovingBlockEntityAccess {

	@Shadow private Direction direction;
	
	@Shadow private float progress;
	@Shadow private float progressO;
	
	@Shadow private int deathTicks;

	@Shadow public abstract float getProgress(float tickDelta);

	@Shadow private static void moveCollidedEntities(Level level, BlockPos pos, float nextProgress, PistonMovingBlockEntity blockEntity) { }
	
	@Shadow private static void moveStuckEntities(Level world, BlockPos pos, float nextProgress, PistonMovingBlockEntity blockEntity) { }

	@Shadow protected abstract BlockState getCollisionRelatedBlockState();
	
	@Shadow private static AABB moveByPositionAndProgress(BlockPos pos, AABB box, PistonMovingBlockEntity blockEntity) { return null; }
	
	@Shadow public abstract Direction getMovementDirection();

	private float gs_actualLastProgress;
	@Unique
	private float gs_nextProgress = 0.0f;
	@Unique
	private boolean gs_wasAdded = false;
	
	/* Number of steps for a full extension (visible / modifiable for mod compatibility) */
	private float gs_numberOfSteps = 2.0f;

	public GSPistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(
		method = "getProgress",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onGetProgressHead(float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (level.isClientSide())
			cir.setReturnValue(gs_getOffsetForProgress(progress, gs_actualLastProgress, tickDelta));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float gs_getOffsetForProgress(float progress, float lastProgress, float tickDelta) {
		if ((isRemoved() || this.deathTicks != 0) && GSMathUtil.equalsApproximate(lastProgress, 1.0f))
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
		method = {"moveCollidedEntities", "moveStuckEntities"},
		at = @At("HEAD")
	)
	private static void onMoveEntitiesHead(Level world, BlockPos pos, float nextProgress, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
		((GSIPistonMovingBlockEntityAccess)blockEntity).gs_setNextProgress(nextProgress);
	}
	
	@ModifyVariable(
		method = "moveCollidedEntities",
		argsOnly = false,
		ordinal = 0,
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;getCollisionRelatedBlockState(" +
				")Lnet/minecraft/world/level/block/state/BlockState;"
		)
	)
	private static double onMoveCollidedEntitiesModifyDeltaProgress(double oldDeltaProgress, Level world, BlockPos pos, float nextProgress, PistonMovingBlockEntity blockEntity) {
		return getDeltaProgress(oldDeltaProgress, blockEntity);
	}
	
	@Inject(
		method = "moveCollidedEntities",
		locals = LocalCapture.CAPTURE_FAILSOFT,
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/world/entity/Entity;setDeltaMovement(" +
					"D" +
					"D" +
					"D" +
				")V"
		)
	)
	private static void onMoveCollidedEntitiesAfterEntitySetVelocity(Level world, BlockPos pos, float nextProgress, PistonMovingBlockEntity blockEntity, CallbackInfo ci, Direction direction, double d, VoxelShape voxelShape, AABB box, List<?> list, List<?> list2, boolean bl, Iterator<?> var12, Entity entity) {
		((GSIEntityAccess)entity).gs_setMovedByPiston(true);
	}

	@ModifyVariable(
			method = "moveStuckEntities",
			argsOnly = false,
			ordinal = 1,
			at = @At(
				value = "INVOKE",
				shift = Shift.BEFORE,
				target =
					"Lnet/minecraft/world/level/Level;getEntities(" +
						"Lnet/minecraft/world/entity/Entity;" +
						"Lnet/minecraft/world/phys/AABB;" +
						"Ljava/util/function/Predicate;" +
					")Ljava/util/List;"
			)
	)
	private static double onMoveStuckEntitiesModifyDeltaProgress(double oldDeltaProgress, Level world, BlockPos pos, float nextProgress, PistonMovingBlockEntity blockEntity) {
		return getDeltaProgress(oldDeltaProgress, blockEntity);
	}
	
	private static double getDeltaProgress(double oldDeltaProgress, PistonMovingBlockEntity blockEntity) {
		if (shouldCorrectPushEntities(blockEntity)) {
			float nextProgress = ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getNextProgress();
			float progress = ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getProgress();
			return ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getOffsetForProgress(nextProgress, progress, 1.0f) -
			       ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getOffsetForProgress(nextProgress, progress, 0.0f);
		}
		
		return oldDeltaProgress;
	}
	
	@Redirect(
		method = "moveByPositionAndProgress",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target =
				"Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;progress:F"
		)
	)
	private static float onOffsetBoxRedirectProgress(PistonMovingBlockEntity blockEntity) {
		if (shouldCorrectPushEntities(blockEntity)) {
			float progress = ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getProgress();
			float nextProgress = ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getNextProgress();
			return ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getOffsetForProgress(nextProgress, progress, 0.0f);
		}
		
		return ((GSIPistonMovingBlockEntityAccess)blockEntity).gs_getProgress();
	}

	@Redirect(
		method = { "getCollisionRelatedBlockState", "getCollisionShape" },
		at = @At(
			value = "FIELD",
			opcode = Opcodes.GETFIELD,
			target =
				"Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;progress:F"
		)
	)
	private float onGetCollisionShapeRedirectProgress(PistonMovingBlockEntity blockEntity) {
		if (shouldCorrectPushEntities(blockEntity))
			return getProgress(1.0f);
		return progress;
	}
	
	@Override
	public void gs_onAdded() {
		if (!gs_wasAdded) {
			if (level.isClientSide() && isPushCorrectionEnabled((PistonMovingBlockEntity)(Object)this)) {
				GSClientController controller = GSClientController.getInstance();
				((GSIMinecraftAccess)controller.getClient()).gs_schedulePistonMovingBlockEntityUpdate(worldPosition);
			}
			gs_wasAdded = true;
		}
	}
	
	@Override
	public void gs_handleScheduledUpdate() {
		if (isPushCorrectionEnabled((PistonMovingBlockEntity)(Object)this)) {
			double deltaProgress = getDeltaProgress(0.0, (PistonMovingBlockEntity)(Object)this);
			if (GSMathUtil.equalsApproximate(deltaProgress, 0.0)) {
				// pushedBlock = Slime or animation type = Pause at Beginning.
				markEntitiesMovedByPiston(Math.min(1.0f / gs_numberOfSteps, 1.0f));
			} else {
				moveCollidedEntities(level, worldPosition, 0.0f, (PistonMovingBlockEntity)(Object)this);
				moveStuckEntities(level, worldPosition, 0.0f, (PistonMovingBlockEntity)(Object)this);
			}
		}
	}
	
	@Unique
	private void markEntitiesMovedByPiston(float stretchAmount) {
		VoxelShape voxelShape = getCollisionRelatedBlockState().getCollisionShape(this.level, this.getBlockPos());
		if (!voxelShape.isEmpty()) {
			AABB box = moveByPositionAndProgress(worldPosition, voxelShape.bounds(), (PistonMovingBlockEntity)(Object)this);
			Direction direction = getMovementDirection();
			
			List<Entity> entities = level.getEntities((Entity)null, PistonMath.getMovementArea(box, direction, stretchAmount).minmax(box));
			if (!entities.isEmpty()) {
				Iterator<Entity> entityItr = entities.iterator();

				while (entityItr.hasNext()) {
					Entity entity = entityItr.next();
					if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
						// The player check is not really required, but if we want to
						// run this method on the server, it is probably a good idea.
						if (!(entity instanceof ServerPlayer))
							((GSIEntityAccess)entity).gs_setMovedByPiston(true);
					}
				}
			}
		}
	}
	
	@Unique
	private static boolean shouldCorrectPushEntities(PistonMovingBlockEntity blockEntity) {
		return isPushCorrectionEnabled(blockEntity) && !blockEntity.getMovedState().is(Blocks.SLIME_BLOCK);
	}
	
	@Unique
	private static boolean isPushCorrectionEnabled(PistonMovingBlockEntity blockEntity) {
		Level world = blockEntity.getLevel();
		if (world == null || !world.isClientSide())
			return false;
		return GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get();
	}

	@Inject(
		method = "loadAdditional",
		at = @At("RETURN")
	)
	private void onLoadAdditional(ValueInput tag, CallbackInfo ci) {
		gs_actualLastProgress = Math.max(0.0f, this.progressO - 1.0f / gs_numberOfSteps);
	}
	
	@Inject(
		method = "tick",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target =
				"Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;progressO:F"
		)
	)
	private static void onTickProgressChanged(Level world, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
		((GSPistonMovingBlockEntityMixin)(Object)blockEntity).gs_actualLastProgress = ((GSPistonMovingBlockEntityMixin)(Object)blockEntity).progressO;
	}

	@Inject(
		method = "finalTick",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target =
				"Lnet/minecraft/world/level/block/piston/PistonMovingBlockEntity;progressO:F"
		)
	)
	private void onFinalTickProgressChanged(CallbackInfo ci) {
		gs_actualLastProgress = this.progressO;
	}
	
	@Override
	public float gs_getProgress() {
		return progress;
	}
	
	@Override
	public float gs_getNextProgress() {
		return gs_nextProgress;
	}
	
	@Override
	public void gs_setNextProgress(float nextProgress) {
		this.gs_nextProgress = nextProgress;
	}
}
