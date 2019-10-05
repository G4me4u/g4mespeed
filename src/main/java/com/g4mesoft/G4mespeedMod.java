package com.g4mesoft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.g4mesoft.packet.GSPacketManager;

import net.fabricmc.api.ModInitializer;

public class G4mespeedMod implements ModInitializer {

	public static final int GS_VERSION_100 = 100;
	
	public static final int GS_VERSION = GS_VERSION_100;
	public static final int INVALID_GS_VERSION = -1;

	public static final Logger GS_LOGGER = LogManager.getLogger("G4mespeed");

	private static G4mespeedMod instance;
	
	private String displayName;
	
	private GSPacketManager packetManager;
	
	@Override
	public void onInitialize() {
		instance = this;

		displayName = "G4mespeed " + getVersionAsString(GS_VERSION);
		
		packetManager = new GSPacketManager();
		
		GS_LOGGER.info(getDisplayName() + " initialized!");
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
