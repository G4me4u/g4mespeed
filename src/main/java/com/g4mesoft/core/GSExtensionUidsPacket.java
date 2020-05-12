package com.g4mesoft.core;

import java.io.IOException;
import java.util.Arrays;

import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSExtensionUidsPacket implements GSIPacket {

	private GSExtensionUID[] extensionUids;
	
	public GSExtensionUidsPacket() {
	}

	public GSExtensionUidsPacket(GSExtensionUID[] extensionUids) {
		this.extensionUids = Arrays.copyOf(extensionUids, extensionUids.length);
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		extensionUids = new GSExtensionUID[buf.readInt()];
		for (int i = 0; i < extensionUids.length; i++)
			extensionUids[i] = GSExtensionUID.read(buf);
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeInt(extensionUids.length);
		for (int i = 0; i < extensionUids.length; i++)
			GSExtensionUID.write(buf, extensionUids[i]);
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
