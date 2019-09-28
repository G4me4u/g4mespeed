package com.g4mesoft.module.probe;

import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.gui.GSTabbedGUI;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSProbeModule implements GSIModule {

	public static final int PROBE_INTRODUCTION_VERSION = 101;
	private static final String PROBE_GUI_TITLE = "Probes";

	@Override
	public void init(GSIModuleManager manager) {
	}

	@Override
	public void tick() {
	}

	@Override
	public void keyReleased(int key, int scancode, int mods) {
	}

	@Override
	public void keyPressed(int key, int scancode, int mods) {
	}

	@Override
	public void keyRepeat(int key, int scancode, int mods) {
	}

	@Override
	public void onJoinG4mespeedServer(int serverVersion) {
	}

	@Override
	public void onDisconnectServer() {
	}
	
	@Override
	public void initGUI(GSTabbedGUI tabbedGUI) {
		tabbedGUI.addTab(PROBE_GUI_TITLE, null);
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
	}

	@Override
	public void onPlayerJoin(ServerPlayerEntity player) {
	}

	@Override
	public void onG4mespeedClientJoin(ServerPlayerEntity player, int version) {
	}

	@Override
	public void onPlayerLeave(ServerPlayerEntity player) {
	}

	@Override
	public void onServerShutdown() {
	}
}
