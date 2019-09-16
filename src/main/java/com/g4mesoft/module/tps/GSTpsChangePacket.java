package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSTpsChangePacket implements GSIPacket {

	private float requestedTps;
	
	public GSTpsChangePacket() {
	}
	
	public GSTpsChangePacket(float requestedTps) {
		this.requestedTps = requestedTps;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		requestedTps = buf.readFloat();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeFloat(requestedTps);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		GSTpsModule tpsModule = controller.getTpsModule();
		if (tpsModule.isPlayerAllowedTpsChange(player))
			controller.getTpsModule().setTps(requestedTps);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.getTpsModule().setTps(requestedTps);
	}
}
