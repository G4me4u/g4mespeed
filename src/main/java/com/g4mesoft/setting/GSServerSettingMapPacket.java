package com.g4mesoft.setting;

import java.io.IOException;

import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSServerSettingMapPacket implements GSIPacket {

	public static final GSVersion DISABLE_SETTING_INTRODUCTION = GSSettingChangePacket.DISABLE_SETTING_INTRODUCTION;
	
	private GSSettingMap settingMap;
	
	public GSServerSettingMapPacket() {
	}

	public GSServerSettingMapPacket(GSSettingMap settingMap) {
		this.settingMap = settingMap;
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		settingMap = new GSSettingMap(GSSettingCategory.read(buf), null);
		settingMap.readSettings(buf);
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		settingMap.getCategory().write(buf);
		settingMap.writeSettings(buf, GSSetting::isActive);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.getServerSettings().onRemoteSettingMapReceived(settingMap);
	}
}
