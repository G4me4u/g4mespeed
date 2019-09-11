package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSIPlayer;

import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class GSServerPlayerEntityMixin implements GSIPlayer {

	private boolean gsInstalled = false;
	private int gsVersion = G4mespeedMod.INVALID_GS_VERSION;
	
	@Override
	public void setG4mespeedInstalled(boolean gsInstalled) {
		this.gsInstalled = gsInstalled;
	}

	@Override
	public boolean isG4mespeedInstalled() {
		return gsInstalled;
	}

	@Override
	public void setG4mespeedVersion(int gsVersion) {
		this.gsVersion = gsVersion;
	}

	@Override
	public int getG4mespeedVersion() {
		return gsVersion;
	}
}
