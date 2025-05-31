package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIEntityAccess;
import com.g4mesoft.core.client.GSClientController;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class GSEntityMixin implements GSIEntityAccess {

	@Shadow public boolean noClip;
	
	@Unique
	private boolean gs_wasMovedByPiston = false;
	@Unique
	private boolean gs_movedByPiston = false;
	
	@Inject(
		method = "move",
		at = @At("HEAD")
	)
	private void onMoveBeforeAdjustMovementForPiston(MoverType moverType, double x, double y, double z, CallbackInfo ci) {
		if (!noClip && moverType == MoverType.PISTON)
			gs_movedByPiston = true;
	}

	@Redirect(
		method = "move",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;getTime()J"
		)
	)
	private long onAdjustMovementForPistonWorldGetTime(World world) {
		if (world.isClient && GSClientController.getInstance().getTpsModule().cCorrectPistonPushing.get()) {
			// Check if we are pushing entities from outside of the tick loop,
			// meaning that the piston movement delta array from the previous
			// tick should be used.
	        if (!((GSIWorldAccess)world).isTickingBlockEntities())
	        	return world.getTime() - 1L;
		}
		
		return world.getTime();
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
