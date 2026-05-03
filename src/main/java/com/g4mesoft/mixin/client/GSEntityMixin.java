package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.core.client.GSClientController;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public class GSEntityMixin implements GSIEntityAccess {

	@Unique
	private boolean gs_wasMovedByPiston = false;
	@Unique
	private boolean gs_movedByPiston = false;
	
	@Inject(
		method = "move",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/world/entity/Entity;limitPistonMovement(" +
					"Lnet/minecraft/world/phys/Vec3;" +
				")Lnet/minecraft/world/phys/Vec3;"
		)
	)
	private void onMoveBeforeAdjustMovementForPiston(CallbackInfo ci) {
		gs_movedByPiston = true;
	}

	@Redirect(
		method = "limitPistonMovement",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;getGameTime()J"
		)
	)
	private long onLimitPistonMovementLevelGetGameTime(Level level) {
		if (level.isClientSide && GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get()) {
			// Check if we are pushing entities from outside of the tick loop,
			// meaning that the piston movement delta array from the previous
			// tick should be used.
	        if (!((GSILevelAccess)level).isTickingBlockEntities())
	        	return level.getGameTime() - 1L;
		}
		
		return level.getGameTime();
	}
	
	@Override
	public void gs_preTick() {
		gs_wasMovedByPiston = gs_movedByPiston;
		gs_movedByPiston = false;
	}
	
	@Override
	public boolean gs_wasMovedByPiston() {
		return gs_wasMovedByPiston;
	}
	
	@Override
	public boolean gs_isMovedByPiston() {
		return gs_movedByPiston;
	}
	
	@Override
	public void gs_setMovedByPiston(boolean movedByPiston) {
		this.gs_movedByPiston = movedByPiston;
	}
}
