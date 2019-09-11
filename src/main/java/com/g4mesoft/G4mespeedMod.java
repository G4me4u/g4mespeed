package com.g4mesoft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.settings.GSSettings;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.ServerCommandSource;

public class G4mespeedMod implements ModInitializer {

	public static final int GS_VERSION = 100;
	public static final int INVALID_GS_VERSION = -1;

	public static final Logger GS_LOGGER = LogManager.getLogger("G4mespeed");
	
	private static G4mespeedMod instance;
	
	private GSSettings settings;
	private GSPacketManager packetManager;
	
	@Override
	public void onInitialize() {
		instance = this;
		
		settings = new GSSettings();
		packetManager = new GSPacketManager();
		
		GS_LOGGER.info("G4mespeed " + getVersionAsString(GS_VERSION) + " initialized!");
	}
	
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
	}
	
	public GSSettings getSettings() {
		return settings;
	}
	
	public GSPacketManager getPacketManager() {
		return packetManager;
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
