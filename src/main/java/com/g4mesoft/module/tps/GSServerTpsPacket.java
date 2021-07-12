package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

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
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
	}

	@Override
	public void handleOnClient(GSClientController controller) {
		controller.getTpsModule().onServerTps(serverTps);
	}
}
