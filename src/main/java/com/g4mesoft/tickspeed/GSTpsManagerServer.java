package com.g4mesoft.tickspeed;

import com.g4mesoft.GSControllerServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class GSTpsManagerServer extends GSTpsManager {

	public GSTpsManagerServer(GSControllerServer controller) {
		super(controller);
	}

	public void resetTps(ServerPlayerEntity player) {
		if (!isPlayerAllowedTpsChange(player))
			return;

		setTps(DEFAULT_TPS, player);
	}

	public void setTps(float tps, ServerPlayerEntity player) {
		if (!isPlayerAllowedTpsChange(player))
			return;

		if (super.setTps(tps)) {
			MinecraftServer server = ((GSControllerServer)controller).getServer();
			String name = player.getEntityName();

			Text text = new TranslatableText("%s changed tps to: %s", name, Float.toString(tps));
			server.getPlayerManager().sendToAll(text.formatted(Formatting.AQUA));
		}
	}

	private boolean isPlayerAllowedTpsChange(ServerPlayerEntity player) {
		return player.allowsPermissionLevel(2);
	}
}
