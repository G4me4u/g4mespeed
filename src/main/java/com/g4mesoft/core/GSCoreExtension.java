package com.g4mesoft.core;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.tps.GSServerSyncPacket;
import com.g4mesoft.module.tps.GSServerTpsPacket;
import com.g4mesoft.module.tps.GSTpsChangePacket;
import com.g4mesoft.module.tps.GSTpsHotkeyPacket;
import com.g4mesoft.module.translation.GSOutdatedTranslationVersionPacket;
import com.g4mesoft.module.translation.GSTranslationCachePacket;
import com.g4mesoft.module.translation.GSTranslationVersionsPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.registry.GSElementRegistry;
import com.g4mesoft.setting.GSServerSettingMapPacket;
import com.g4mesoft.setting.GSSettingChangePacket;
import com.g4mesoft.setting.GSSettingPermissionPacket;

public class GSCoreExtension implements GSIExtension {

	private static final String TRANSLATION_PATH = "/assets/g4mespeed/lang/en.lang";
	
	@Override
	public void init() {
	}
	
	@Override
	public void registerPackets(GSElementRegistry<GSIPacket> registry) {
		registry.register(0, GSVersionPacket.class, GSVersionPacket::new);
		
		registry.register(1, GSTpsHotkeyPacket.class, GSTpsHotkeyPacket::new);
		registry.register(2, GSTpsChangePacket.class, GSTpsChangePacket::new);
		registry.register(3, GSServerSyncPacket.class, GSServerSyncPacket::new);
		
		registry.register(4, GSOutdatedTranslationVersionPacket.class, GSOutdatedTranslationVersionPacket::new);
		registry.register(5, GSTranslationCachePacket.class, GSTranslationCachePacket::new);

		registry.register(6, GSServerSettingMapPacket.class, GSServerSettingMapPacket::new);
		registry.register(7, GSSettingChangePacket.class, GSSettingChangePacket::new);
		registry.register(8, GSSettingPermissionPacket.class, GSSettingPermissionPacket::new);
		
		registry.register(9, GSTranslationVersionsPacket.class, GSTranslationVersionsPacket::new);
		registry.register(10, GSExtensionUidsPacket.class, GSExtensionUidsPacket::new);
		registry.register(11, GSServerTpsPacket.class, GSServerTpsPacket::new);
	}

	@Override
	public void addClientModules(GSControllerClient controller) {
	}

	@Override
	public void addServerModules(GSControllerServer controller) {
	}

	@Override
	public String getTranslationPath() {
		return TRANSLATION_PATH;
	}

	@Override
	public String getName() {
		return G4mespeedMod.CORE_MOD_NAME;
	}

	@Override
	public GSExtensionUID getUniqueId() {
		return G4mespeedMod.CORE_EXTENSION_UID;
	}
}
