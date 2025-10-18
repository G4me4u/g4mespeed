package com.g4mesoft.mixin.common;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSServerController;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
public abstract class GSPlayerManagerMixin {

	@Shadow public abstract ServerPlayerEntity getPlayer(UUID uuid);
	
	@Inject(
		method = "onPlayerConnect",
		at = @At("RETURN")
	)
	private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
		GSServerController.getInstance().onPlayerJoin(player);
	}

	@Inject(
		method = "remove",
		at = @At("HEAD")
	)
	private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
		GSServerController.getInstance().onPlayerLeave(player);
	}

	@Inject(
		method = "addToOperators",
		at = @At("RETURN")
	)
	private void onAddToOperators(PlayerConfigEntry entry, CallbackInfo ci) {
		onPlayerPermissionChanged(entry);
	}

	@Inject(
		method = "removeFromOperators",
		at = @At("RETURN")
	)
	private void onRemoveFromOperators(PlayerConfigEntry entry, CallbackInfo ci) {
		onPlayerPermissionChanged(entry);
	}
	
	@Unique
	private void onPlayerPermissionChanged(PlayerConfigEntry entry) {
		// We could capture the local variable, however,
		// doing so might not be feasible if other mods
		// inject the same method.
		ServerPlayerEntity player = this.getPlayer(entry.id());
		if (player != null)
			GSServerController.getInstance().onPlayerPermissionChanged(player);
	}
}
