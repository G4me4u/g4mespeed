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

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class GSEntityMixin implements GSIEntityAccess {

	@Unique
	private boolean gs_wasMovedByPiston = false;
	@Unique
	private boolean gs_movedByPiston = false;
	
	@Inject(method = "move", at = @At(value = "INVOKE", shift = Shift.BEFORE,
	        target = "Lnet/minecraft/entity/Entity;adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
	private void onMoveBeforeAdjustMovementForPiston(CallbackInfo ci) {
		gs_movedByPiston = true;
	}

	@Inject(method = "resetPosition", at = @At("HEAD"))
	private void onResetPosition(CallbackInfo ci) {
		gs_wasMovedByPiston = gs_movedByPiston;
		gs_movedByPiston = false;
	}

	@Redirect(method = "adjustMovementForPiston", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTime()J"))
	private long onAdjustMovementForPistonWorldGetTime(World world) {
		if (world.isClient && GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get()) {
			// Check if we are pushing entities from outside of the tick loop,
			// meaning that the piston movement delta array from the previous
			// tick should be used.
	        if (!((GSIWorldAccess)world).isIteratingTickingBlockEntities())
	        	return world.getTime() - 1L;
		}
		
		return world.getTime();
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
