package com.g4mesoft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSPacketManager;

import net.fabricmc.api.ModInitializer;

public class G4mespeedMod implements ModInitializer {

	public static final String MOD_NAME = "G4mespeed";
	public static final GSVersion GS_VERSION = new GSVersion(1, 0, 1);

	public static final Logger GS_LOGGER = LogManager.getLogger("G4mespeed");

	private static G4mespeedMod instance;
	
	private GSPacketManager packetManager;
	
	@Override
	public void onInitialize() {
		instance = this;

		packetManager = new GSPacketManager();
		
		GS_LOGGER.info("G4mespeed " + GS_VERSION.getVersionString() + " initialized!");
	}
	
	public GSPacketManager getPacketManager() {
		return packetManager;
	}

	public static G4mespeedMod getInstance() {
		return instance;
	}
}
