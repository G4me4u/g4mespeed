package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
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

	private boolean wasMovedByPiston = false;
	private boolean movedByPiston = false;
	
	@Inject(method = "move", at = @At(value = "INVOKE", shift = Shift.BEFORE,
	        target = "Lnet/minecraft/entity/Entity;adjustMovementForPiston(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;"))
	private void onMoveBeforeAdjustMovementForPiston(CallbackInfo ci) {
		movedByPiston = true;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(CallbackInfo ci) {
		wasMovedByPiston = movedByPiston;
		movedByPiston = false;
	}

	@Redirect(method = "adjustMovementForPiston", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTime()J"))
	private long onAdjustMovementForPistonWorldGetTime(World world) {
		if (world.isClient && GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.getValue()) {
			// Check if we are pushing entities from outside of the tick loop,
			// meaning that the piston movement delta array from the previous
			// tick should be used.
	        if (!((GSIWorldAccess)world).isIteratingTickingBlockEntities())
	        	return world.getTime() - 1L;
		}
		
		return world.getTime();
	}
	
	@Override
	public boolean wasMovedByPiston() {
		return wasMovedByPiston;
	}
	
	@Override
	public boolean isMovedByPiston() {
		return movedByPiston;
	}
	
	@Override
	public void setMovedByPiston(boolean movedByPiston) {
		this.movedByPiston = movedByPiston;
	}
}
