package com.g4mesoft.core;

import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.setting.GSSettingManager;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIModule {

	public void init(GSIModuleManager manager);

	default public void onClose() { }

	@Environment(EnvType.CLIENT)
	default public void registerClientSettings(GSSettingManager settings) { }

	default public void registerServerSettings(GSSettingManager settings) { }
	
	default public void tick() { }
	
	@Environment(EnvType.CLIENT)
	default public void keyReleased(int key, int scancode, int mods) { }

	@Environment(EnvType.CLIENT)
	default public void keyPressed(int key, int scancode, int mods) { }

	@Environment(EnvType.CLIENT)
	default public void keyRepeat(int key, int scancode, int mods) { }

	@Environment(EnvType.CLIENT)
	default public void onJoinG4mespeedServer(GSVersion serverVersion) { }

	@Environment(EnvType.CLIENT)
	default public void onDisconnectServer() { }

	@Environment(EnvType.CLIENT)
	default public void initGUI(GSTabbedGUI tabbedGUI) { }

	@Environment(EnvType.CLIENT)
	
	default public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) { }

	default public void onPlayerJoin(ServerPlayerEntity player) { }

	default public void onG4mespeedClientJoin(ServerPlayerEntity player, GSVersion version) { }

	default public void onPlayerLeave(ServerPlayerEntity player) { }

}
