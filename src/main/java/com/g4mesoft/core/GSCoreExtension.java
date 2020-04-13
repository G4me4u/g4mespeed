package com.g4mesoft.core;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.tps.GSServerSyncPacket;
import com.g4mesoft.module.tps.GSTpsChangePacket;
import com.g4mesoft.module.tps.GSTpsHotkeyPacket;
import com.g4mesoft.module.translation.GSTranslationCachePacket;
import com.g4mesoft.module.translation.GSTranslationVersionPacket;
import com.g4mesoft.packet.GSPacketRegistry;
import com.g4mesoft.setting.GSServerSettingMapPacket;
import com.g4mesoft.setting.GSSettingChangePacket;
import com.g4mesoft.setting.GSSettingPermissionPacket;

public class GSCoreExtension implements GSIExtension {

	private static final byte CORE_MOD_UID = 0x00b;
	
	@Override
	public void registerPackets(GSPacketRegistry registry) {
		// Core packets
		registry.register(0, GSVersionPacket.class, GSVersionPacket::new);
		
		// Tps packets
		registry.register(1, GSTpsHotkeyPacket.class, GSTpsHotkeyPacket::new);
		registry.register(2, GSTpsChangePacket.class, GSTpsChangePacket::new);
		registry.register(3, GSServerSyncPacket.class, GSServerSyncPacket::new);
		
		// Translation packets
		registry.register(4, GSTranslationVersionPacket.class, GSTranslationVersionPacket::new);
		registry.register(5, GSTranslationCachePacket.class, GSTranslationCachePacket::new);

		// Setting packets
		registry.register(6, GSServerSettingMapPacket.class, GSServerSettingMapPacket::new);
		registry.register(7, GSSettingChangePacket.class, GSSettingChangePacket::new);
		registry.register(8, GSSettingPermissionPacket.class, GSSettingPermissionPacket::new);
	}

	@Override
	public void addClientModules(GSControllerClient client) {
	}

	@Override
	public void addServerModules(GSControllerServer server) {
	}

	@Override
	public String getName() {
		return G4mespeedMod.MOD_NAME;
	}

	@Override
	public byte getUniqueId() {
		return CORE_MOD_UID;
	}
}
