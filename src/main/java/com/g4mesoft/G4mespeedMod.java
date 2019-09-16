package com.g4mesoft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.settings.GSGlobalSettings;

import net.fabricmc.api.ModInitializer;

public class G4mespeedMod implements ModInitializer {

	public static final int GS_VERSION = 100;
	public static final int INVALID_GS_VERSION = -1;

	public static final Logger GS_LOGGER = LogManager.getLogger("G4mespeed");
	
	private static G4mespeedMod instance;
	
	private String displayName;
	
	private GSGlobalSettings settings;
	private GSPacketManager packetManager;
	
	@Override
	public void onInitialize() {
		instance = this;

		displayName = "G4mespeed " + getVersionAsString(GS_VERSION);
		
		settings = new GSGlobalSettings();
		packetManager = new GSPacketManager();
		
		GS_LOGGER.info(getDisplayName() + " initialized!");
	}
	
	public GSGlobalSettings getSettings() {
		return settings;
	}
	
	public GSPacketManager getPacketManager() {
		return packetManager;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public static String getVersionAsString(int version) {
		return String.format("%d.%d", version / 100, version % 100);
	}
	
	public static final int getMajorVersion() {
		return GS_VERSION / 100;
	}

	public static final int getMinorVersion() {
		return GS_VERSION % 100;
	}

	public static G4mespeedMod getInstance() {
		return instance;
	}
}
