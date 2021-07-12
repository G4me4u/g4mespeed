package com.g4mesoft.core;

import java.io.IOException;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class GSConnectionPacket implements GSIPacket {

	private GSExtensionInfo[] extensionInfo;
	
	public GSConnectionPacket() {
	}

	public GSConnectionPacket(GSExtensionInfoList infoList) {
		// Copy the extension info list to ensure thread-safety.
		this.extensionInfo = infoList.getAllInfo().toArray(new GSExtensionInfo[0]);
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		extensionInfo = new GSExtensionInfo[buf.readInt()];
		for (int i = 0; i < extensionInfo.length; i++)
			extensionInfo[i] = GSExtensionInfo.read(buf);
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeInt(extensionInfo.length);
		for (int i = 0; i < extensionInfo.length; i++)
			GSExtensionInfo.write(buf, extensionInfo[i]);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
		controller.onG4mespeedClientJoined(player, extensionInfo);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller) {
		controller.onJoinG4mespeedServer(extensionInfo);
	}
}
