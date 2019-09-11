package com.g4mesoft.core;

import java.io.IOException;

import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSVersionPacket implements GSIPacket {

	private int version;

	public GSVersionPacket() {
	}
	
	public GSVersionPacket(int version) {
		this.version = version;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		version = buf.readInt();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeInt(version);
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
