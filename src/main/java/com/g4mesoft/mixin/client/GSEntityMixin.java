package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.g4mesoft.access.client.GSIEntityAccess;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public class GSEntityMixin implements GSIEntityAccess {

	@Unique
	private boolean gs_wasMovedByPiston = false;
	@Unique
	private boolean gs_movedByPiston = false;
	
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
