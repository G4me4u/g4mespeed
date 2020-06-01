package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSServerTpsPacket implements GSIPacket {

	private float serverTps;

	public GSServerTpsPacket() {
	}
	
	public GSServerTpsPacket(float serverTps) {
		this.serverTps = serverTps;
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		serverTps = buf.readFloat();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeFloat(serverTps);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	public void handleOnClient(GSControllerClient controller) {
		controller.getTpsModule().onServerTps(serverTps);
	}
}
