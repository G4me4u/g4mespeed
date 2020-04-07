package com.g4mesoft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.packet.GSPacketManager;

import net.fabricmc.api.ModInitializer;

public class G4mespeedMod implements ModInitializer {

	public static final String MOD_NAME = "G4mespeed";
	public static final GSVersion GS_VERSION = new GSVersion(1, 0, 6);

	public static final Logger GS_LOGGER = LogManager.getLogger(MOD_NAME);

	private static G4mespeedMod instance;
	
	private GSPacketManager packetManager;
	
	private GSCarpetCompat carpetCompat;
	
	@Override
	public void onInitialize() {
		instance = this;

		packetManager = new GSPacketManager();
		
		carpetCompat = new GSCarpetCompat();
		carpetCompat.detectCarpet();
		
		GS_LOGGER.info("G4mespeed " + GS_VERSION.getVersionString() + " initialized!");
	}
	
	public GSPacketManager getPacketManager() {
		return packetManager;
	}

	public GSCarpetCompat getCarpetCompat() {
		return carpetCompat;
	}

	public static G4mespeedMod getInstance() {
		return instance;
	}
}
