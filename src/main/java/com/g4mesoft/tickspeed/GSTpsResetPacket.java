package com.g4mesoft.tickspeed;

import java.io.IOException;

import com.g4mesoft.GSControllerClient;
import com.g4mesoft.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSTpsResetPacket implements GSIPacket {

	public GSTpsResetPacket() {
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		controller.getTpsManager().resetTps(player);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.getTpsManager().resetTps();
	}
}
