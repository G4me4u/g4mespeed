package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.level.GameType;

@Mixin(MultiPlayerGameMode.class)
public class GSMultiPlayerGameModeMixin {

	@Inject(
		method =
			"setLocalMode(" +
				"Lnet/minecraft/world/level/GameType;" +
				"Lnet/minecraft/world/level/GameType;" +
			")V",
		at = @At("RETURN")
	)
    public void onSetLocalModeWithPrev(GameType gameMode, GameType previousGameMode, CallbackInfo ci) {
		GSClientController.getInstance().getTpsModule().onClientGameModeChanged(gameMode);
	}

	@Inject(
		method =
			"setLocalMode(" +
				"Lnet/minecraft/world/level/GameType;" +
			")V",
		at = @At("RETURN")
	)
    private void onSetLocalMode(GameType gameMode, CallbackInfo ci) {
		GSClientController.getInstance().getTpsModule().onClientGameModeChanged(gameMode);
	}
}
