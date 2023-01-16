package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSServerSyncPacket implements GSIPacket {

	private int packetInterval;
	
	public GSServerSyncPacket() {
	}

	public GSServerSyncPacket(int packetInterval) {
		if (packetInterval > 0xFF)
			throw new IllegalArgumentException("packetInterval too large");
		this.packetInterval = packetInterval;
	}
	
	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		packetInterval = buf.readUnsignedByte();
	}

	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		buf.writeUnsignedByte((short)packetInterval);
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
