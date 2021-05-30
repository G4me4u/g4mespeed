package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.g4mesoft.access.GSIAbstractClientPlayerEntityAccess;

import net.minecraft.client.network.AbstractClientPlayerEntity;

@Mixin(AbstractClientPlayerEntity.class)
public class GSAbstractClientPlayerEntityMixin implements GSIAbstractClientPlayerEntityAccess {

	private boolean fixedMovement;
	
	@Override
	public boolean isFixedMovement() {
		return fixedMovement;
	}

	@Override
	public void setFixedMovement(boolean fixedMovement) {
		this.fixedMovement = fixedMovement;
	}
}
