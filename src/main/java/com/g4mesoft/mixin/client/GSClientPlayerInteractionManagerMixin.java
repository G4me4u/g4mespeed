package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;

import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.world.WorldSettings;

@Mixin(ClientPlayerInteractionManager.class)
public class GSClientPlayerInteractionManagerMixin {

	@Inject(
		method = "setGameMode",
		at = @At("RETURN")
	)
    private void onSetGameMode(WorldSettings.GameMode gameMode, CallbackInfo ci) {
		GSClientController.getInstance().getTpsModule().onClientGameModeChanged(gameMode);
	}
}
