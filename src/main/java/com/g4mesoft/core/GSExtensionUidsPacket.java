package com.g4mesoft.core;

import java.io.IOException;
import java.util.Arrays;

import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSExtensionUidsPacket implements GSIPacket {

	private byte[] extensionUids;
	
	public GSExtensionUidsPacket() {
	}

	public GSExtensionUidsPacket(byte[] extensionUids) {
		this.extensionUids = Arrays.copyOf(extensionUids, extensionUids.length);
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		extensionUids = buf.readByteArray();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeByteArray(extensionUids);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		((GSINetworkHandlerAccess)player.networkHandler).setExtensionUids(extensionUids);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.setServerExtensionUids(extensionUids);
	}
}
