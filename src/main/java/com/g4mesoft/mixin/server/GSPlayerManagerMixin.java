package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.GSControllerServer;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public class GSPlayerManagerMixin {

	@Inject(method = "onPlayerConnect", at = @At("RETURN"))
	public void onPlayerJoin(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci) {
		GSControllerServer.getInstance().onPlayerJoin(player);
	}

	@Inject(method = "remove", at = @At("HEAD"))
	public void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
		GSControllerServer.getInstance().onPlayerLeave(player);
	}
}
