package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.g4mesoft.access.client.GSIAbstractClientPlayerAccess;

import net.minecraft.client.player.AbstractClientPlayer;

@Mixin(AbstractClientPlayer.class)
public class GSAbstractClientPlayerMixin implements GSIAbstractClientPlayerAccess {

	@Unique
	private boolean gs_fixedMovement;
	
	@Override
	public boolean gs_isFixedMovement() {
		return gs_fixedMovement;
	}

	@Override
	public void gs_setFixedMovement(boolean fixedMovement) {
		this.gs_fixedMovement = fixedMovement;
	}
}
