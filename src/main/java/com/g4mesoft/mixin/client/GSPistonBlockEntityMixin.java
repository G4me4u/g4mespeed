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
import com.g4mesoft.access.client.GSIMinecraftClientAccess;
import com.g4mesoft.access.client.GSIPistonBlockEntityAccess;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Boxes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

@Mixin(PistonBlockEntity.class)
public abstract class GSPistonBlockEntityMixin extends BlockEntity implements GSIPistonBlockEntityAccess {

	@Shadow private Direction facing;
	
	@Shadow private float progress;
	@Shadow private float lastProgress;
	@Shadow private BlockState pushedBlock;
	
	@Shadow private int field_26705;

	@Shadow public abstract float getProgress(float tickDelta);

	@Shadow private static void pushEntities(World world, BlockPos pos, float nextProgress, PistonBlockEntity blockEntity) { }
	
	@Shadow private static void moveEntitiesInHoneyBlock(World world, BlockPos pos, float nextProgress, PistonBlockEntity blockEntity) { }

	@Shadow protected abstract BlockState getHeadBlockState();
	
	@Shadow private static Box offsetHeadBox(BlockPos pos, Box box, PistonBlockEntity blockEntity) { return null; }
	
	@Shadow public abstract Direction getMovementDirection();

	private float gs_actualLastProgress;
	@Unique
	private float gs_nextProgress = 0.0f;
	@Unique
	private boolean gs_wasAdded = false;
	
	/* Number of steps for a full extension (visible / modifiable for mod compatibility) */
	private float gs_numberOfSteps = 2.0f;

	public GSPistonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Inject(method = "getProgress", cancellable = true, at = @At("HEAD"))
	private void onGetProgressHead(float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (world.isClient)
			cir.setReturnValue(gs_getOffsetForProgress(progress, gs_actualLastProgress, tickDelta));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float gs_getOffsetForProgress(float progress, float lastProgress, float tickDelta) {
		if ((isRemoved() || this.field_26705 != 0) && GSMathUtil.equalsApproximate(lastProgress, 1.0f))
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

	@Inject(method = {"pushEntities", "moveEntitiesInHoneyBlock"}, at = @At("HEAD"))
	private static void onMoveEntitiesHead(World world, BlockPos pos, float nextProgress, PistonBlockEntity blockEntity, CallbackInfo ci) {
		((GSIPistonBlockEntityAccess)blockEntity).gs_setNextProgress(nextProgress);
	}
	
	@ModifyVariable(method = "pushEntities", argsOnly = false, ordinal = 0, at = @At(value = "INVOKE", shift = Shift.BEFORE,
	                target = "Lnet/minecraft/block/entity/PistonBlockEntity;getHeadBlockState()Lnet/minecraft/block/BlockState;"))
	private static double onPushEntitiesModifyDeltaProgress(double oldDeltaProgress, World world, BlockPos pos, float nextProgress, PistonBlockEntity blockEntity) {
		return getDeltaProgress(oldDeltaProgress, blockEntity);
	}
	
	@Inject(method = "pushEntities", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lnet/minecraft/entity/Entity;setVelocity(DDD)V"))
	private static void onPushEntitiesAfterEntitySetVelocity(World world, BlockPos pos, float nextProgress, PistonBlockEntity blockEntity, CallbackInfo ci, Direction direction, double d, VoxelShape voxelShape, Box box, List<?> list, List<?> list2, boolean bl, Iterator<?> var12, Entity entity) {
		((GSIEntityAccess)entity).gs_setMovedByPiston(true);
	}

	@ModifyVariable(method = "moveEntitiesInHoneyBlock", argsOnly = false, ordinal = 1, at = @At(value = "INVOKE", shift = Shift.BEFORE,
	                target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
	private static double onMethod_23674ModifyDeltaProgress(double oldDeltaProgress, World world, BlockPos pos, float nextProgress, PistonBlockEntity blockEntity) {
		return getDeltaProgress(oldDeltaProgress, blockEntity);
	}
	
	private static double getDeltaProgress(double oldDeltaProgress, PistonBlockEntity blockEntity) {
		if (shouldCorrectPushEntities(blockEntity)) {
			float nextProgress = ((GSIPistonBlockEntityAccess)blockEntity).gs_getNextProgress();
			float progress = ((GSIPistonBlockEntityAccess)blockEntity).gs_getProgress();
			return ((GSIPistonBlockEntityAccess)blockEntity).gs_getOffsetForProgress(nextProgress, progress, 1.0f) -
			       ((GSIPistonBlockEntityAccess)blockEntity).gs_getOffsetForProgress(nextProgress, progress, 0.0f);
		}
		
		return oldDeltaProgress;
	}
	
	@Redirect(method = "offsetHeadBox", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
	          target = "Lnet/minecraft/block/entity/PistonBlockEntity;progress:F"))
	private static float onOffsetBoxRedirectProgress(PistonBlockEntity blockEntity) {
		if (shouldCorrectPushEntities(blockEntity)) {
			float progress = ((GSIPistonBlockEntityAccess)blockEntity).gs_getProgress();
			float nextProgress = ((GSIPistonBlockEntityAccess)blockEntity).gs_getNextProgress();
			return ((GSIPistonBlockEntityAccess)blockEntity).gs_getOffsetForProgress(nextProgress, progress, 0.0f);
		}
		
		return ((GSIPistonBlockEntityAccess)blockEntity).gs_getProgress();
	}

	@Redirect(method = { "getHeadBlockState", "getCollisionShape" }, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
	          target = "Lnet/minecraft/block/entity/PistonBlockEntity;progress:F"))
	private float onGetCollisionShapeRedirectProgress(PistonBlockEntity blockEntity) {
		if (shouldCorrectPushEntities(blockEntity))
			return getProgress(1.0f);
		return progress;
	}
	
	@Override
	public void gs_onAdded() {
		if (!gs_wasAdded) {
			if (world.isClient && isPushCorrectionEnabled((PistonBlockEntity)(Object)this)) {
				GSClientController controller = GSClientController.getInstance();
				((GSIMinecraftClientAccess)controller.getClient()).gs_schedulePistonBlockEntityUpdate(pos);
			}
			gs_wasAdded = true;
		}
	}
	
	@Override
	public void gs_handleScheduledUpdate() {
		if (isPushCorrectionEnabled((PistonBlockEntity)(Object)this)) {
			double deltaProgress = getDeltaProgress(0.0, (PistonBlockEntity)(Object)this);
			if (GSMathUtil.equalsApproximate(deltaProgress, 0.0)) {
				// pushedBlock = Slime or animation type = Pause at Beginning.
				markEntitiesMovedByPiston(Math.min(1.0f / gs_numberOfSteps, 1.0f));
			} else {
				pushEntities(world, pos, 0.0f, (PistonBlockEntity)(Object)this);
				moveEntitiesInHoneyBlock(world, pos, 0.0f, (PistonBlockEntity)(Object)this);
			}
		}
	}
	
	@Unique
	private void markEntitiesMovedByPiston(float stretchAmount) {
		VoxelShape voxelShape = getHeadBlockState().getCollisionShape(this.world, this.getPos());
		if (!voxelShape.isEmpty()) {
			Box box = offsetHeadBox(pos, voxelShape.getBoundingBox(), (PistonBlockEntity)(Object)this);
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
							((GSIEntityAccess)entity).gs_setMovedByPiston(true);
					}
				}
			}
		}
	}
	
	@Unique
	private static boolean shouldCorrectPushEntities(PistonBlockEntity blockEntity) {
		return isPushCorrectionEnabled(blockEntity) && !blockEntity.getPushedBlock().isOf(Blocks.SLIME_BLOCK);
	}
	
	@Unique
	private static boolean isPushCorrectionEnabled(PistonBlockEntity blockEntity) {
		World world = blockEntity.getWorld();
		if (world == null || !world.isClient)
			return false;
		return GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get();
	}

	@Inject(method = "readNbt", at = @At("RETURN"))
	private void onReadNbt(NbtCompound tag, CallbackInfo ci) {
		gs_actualLastProgress = Math.max(0.0f, this.lastProgress - 1.0f / gs_numberOfSteps);
	}
	
	@Inject(method = "tick", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private static void onTickProgressChanged(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity, CallbackInfo ci) {
		((GSPistonBlockEntityMixin)(Object)blockEntity).gs_actualLastProgress = ((GSPistonBlockEntityMixin)(Object)blockEntity).lastProgress;
	}

	@Inject(method = "finish", at = @At(value = "FIELD", target="Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onFinishProgressChanged(CallbackInfo ci) {
		gs_actualLastProgress = this.lastProgress;
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
