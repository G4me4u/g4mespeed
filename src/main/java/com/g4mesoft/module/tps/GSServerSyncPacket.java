package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class GSServerSyncPacket implements GSIPacket {

	private int packetInterval;
	
	public GSServerSyncPacket() {
	}

	public GSServerSyncPacket(int packetInterval) {
		this.packetInterval = packetInterval;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		packetInterval = buf.readVarInt();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeVarInt(packetInterval);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller) {
		controller.getTpsModule().onServerSyncPacket(packetInterval);
	}
	
	@Override
	public boolean shouldForceMainThread() {
		return false;
	}
}
