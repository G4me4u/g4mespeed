package com.g4mesoft.core;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSFlushingBlockEntityUpdatesPacket;
import com.g4mesoft.module.tps.GSPlayerFixedMovementPacket;
import com.g4mesoft.module.tps.GSServerPlayerFixedMovementPacket;
import com.g4mesoft.module.tps.GSServerSyncPacket;
import com.g4mesoft.module.tps.GSServerTpsPacket;
import com.g4mesoft.module.tps.GSTpsChangePacket;
import com.g4mesoft.module.tps.GSTpsHotkeyPacket;
import com.g4mesoft.module.translation.GSTranslationCachePacket;
import com.g4mesoft.module.translation.GSTranslationVersionsPacket;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.registry.GSSupplierRegistry;
import com.g4mesoft.setting.GSServerSettingMapPacket;
import com.g4mesoft.setting.GSSettingChangePacket;
import com.g4mesoft.setting.GSSettingPermissionPacket;

public class GSCoreExtension implements GSIExtension {

	public static final String NAME        = "Core";
	/* "CORE" in ASCII as HEX */
	public static final GSExtensionUID UID = new GSExtensionUID(0x434F5245);
	public static final GSVersion VERSION  = new GSVersion(1, 4, 7);
	
	public static final GSExtensionInfo INFO = new GSExtensionInfo(NAME, UID, VERSION);
	public static final GSExtensionInfo INVALID_VERSION_INFO = new GSExtensionInfo(NAME, UID, GSVersion.INVALID);
	
	private static final String TRANSLATION_PATH = "/assets/g4mespeed/lang/en.lang";
	
	@Override
	public void init() {
	}
	
	@Override
	public void registerPackets(GSSupplierRegistry<Integer, GSIPacket> registry) {
		registry.register( 1, GSTpsHotkeyPacket.class, GSTpsHotkeyPacket::new);
		registry.register( 2, GSTpsChangePacket.class, GSTpsChangePacket::new);
		registry.register( 3, GSServerSyncPacket.class, GSServerSyncPacket::new);
		
		registry.register( 5, GSTranslationCachePacket.class, GSTranslationCachePacket::new);

		registry.register( 6, GSServerSettingMapPacket.class, GSServerSettingMapPacket::new);
		registry.register( 7, GSSettingChangePacket.class, GSSettingChangePacket::new);
		registry.register( 8, GSSettingPermissionPacket.class, GSSettingPermissionPacket::new);
		
		registry.register( 9, GSTranslationVersionsPacket.class, GSTranslationVersionsPacket::new);
		registry.register(10, GSConnectionPacket.class, GSConnectionPacket::new);
		registry.register(11, GSServerTpsPacket.class, GSServerTpsPacket::new);
		registry.register(12, GSPlayerFixedMovementPacket.class, GSPlayerFixedMovementPacket::new);
		registry.register(13, GSServerPlayerFixedMovementPacket.class, GSServerPlayerFixedMovementPacket::new);
		
		registry.register(14, GSFlushingBlockEntityUpdatesPacket.class, GSFlushingBlockEntityUpdatesPacket::new);
	}

	@Override
	public void addClientModules(GSClientController controller) {
	}

	@Override
	public void addServerModules(GSServerController controller) {
	}

	@Override
	public String getTranslationPath() {
		return TRANSLATION_PATH;
	}

	@Override
	public GSExtensionInfo getInfo() {
		return INFO;
	}
}
