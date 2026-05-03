package com.g4mesoft.mixin.common;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSServerController;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public abstract class GSPlayerListMixin {

	@Shadow public abstract ServerPlayer getPlayer(UUID uuid);
	
	@Inject(
		method = "placeNewPlayer",
		at = @At("RETURN")
	)
	private void onPlaceNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
		GSServerController.getInstance().onPlayerJoin(player);
	}

	@Inject(
		method = "remove",
		at = @At("HEAD")
	)
	private void onRemove(ServerPlayer player, CallbackInfo ci) {
		GSServerController.getInstance().onPlayerLeave(player);
	}

	@Inject(
		method = "op",
		at = @At("RETURN")
	)
	private void onOp(NameAndId nameAndId, CallbackInfo ci) {
		onPlayerPermissionChanged(nameAndId);
	}

	@Inject(
		method = "deop",
		at = @At("RETURN")
	)
	private void onDeop(NameAndId nameAndId, CallbackInfo ci) {
		onPlayerPermissionChanged(nameAndId);
	}
	
	@Unique
	private void onPlayerPermissionChanged(NameAndId nameAndId) {
		// We could capture the local variable, however,
		// doing so might not be feasible if other mods
		// inject the same method.
		ServerPlayer player = this.getPlayer(nameAndId.id());
		if (player != null)
			GSServerController.getInstance().onPlayerPermissionChanged(player);
	}
}
