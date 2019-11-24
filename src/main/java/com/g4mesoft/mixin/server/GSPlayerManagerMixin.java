package com.g4mesoft.mixin.server;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSControllerServer;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public abstract class GSPlayerManagerMixin {

	@Shadow public abstract ServerPlayerEntity getPlayer(UUID uuid);
	
	@Inject(method = "onPlayerConnect", at = @At("RETURN"))
	public void onPlayerJoin(ClientConnection clientConnection, ServerPlayerEntity player, CallbackInfo ci) {
		GSControllerServer.getInstance().onPlayerJoin(player);
	}

	@Inject(method = "remove", at = @At("HEAD"))
	public void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
		GSControllerServer.getInstance().onPlayerLeave(player);
	}

	@Inject(method = "addToOperators", at = @At("RETURN"))
	public void onAddToOperators(GameProfile gameProfile, CallbackInfo ci) {
		onPlayerPermissionChanged(gameProfile);
	}

	@Inject(method = "removeFromOperators", at = @At("RETURN"))
	public void onRemoveFromOperators(GameProfile gameProfile, CallbackInfo ci) {
		onPlayerPermissionChanged(gameProfile);
	}
	
	private void onPlayerPermissionChanged(GameProfile gameProfile) {
		// We could capture the local variable, however,
		// doing so might not be feasible if other mods
		// inject the same method.
		ServerPlayerEntity player = this.getPlayer(gameProfile.getId());
		if (player != null)
			GSControllerServer.getInstance().onPlayerPermissionChanged(player);
	}
}
