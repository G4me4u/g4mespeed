package com.g4mesoft.setting;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSServerSettingMapPacket implements GSIPacket {

	private GSSettingMap settingMap;
	
	public GSServerSettingMapPacket() {
	}

	public GSServerSettingMapPacket(GSSettingMap settingMap) {
		this.settingMap = settingMap;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		settingMap = new GSSettingMap(GSSettingCategory.read(buf));
		settingMap.readSettings(buf);
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		settingMap.getCategory().write(buf);
		settingMap.writeSettings(buf);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	public void handleOnClient(GSControllerClient controller) {
		controller.getServerSettings().onRemoteSettingMapReceived(settingMap);
	}
}
