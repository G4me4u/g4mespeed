package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

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
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		GSRenderTickCounterAdjuster.getInstance().onServerTickSync(packetInterval);
	}
	
	@Override
	public boolean shouldForceMainThread() {
		return false;
	}
}
