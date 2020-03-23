package com.g4mesoft.core;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class GSVersionPacket implements GSIPacket {

	private GSVersion version;

	public GSVersionPacket() {
	}
	
	public GSVersionPacket(GSVersion version) {
		this.version = version;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		version = GSVersion.read(buf);
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		version.write(buf);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		controller.onG4mespeedClientJoined(player, version);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.onJoinG4mespeedServer(version);
	}
}
