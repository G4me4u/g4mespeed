package com.g4mesoft.packet;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public interface GSIPacket {

	public void read(PacketByteBuf buf) throws IOException;

	public void write(PacketByteBuf buf) throws IOException;
	
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player);

	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller);
	
}
