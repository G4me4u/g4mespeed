package com.g4mesoft.core;

import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.setting.GSSettingManager;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIModule {

	public void init(GSIModuleManager manager);

	@Environment(EnvType.CLIENT)
	default public void initGUI(GSTabbedGUI tabbedGUI) { }
	
	default public void onClose() { }

	@Environment(EnvType.CLIENT)
	default public void registerClientSettings(GSSettingManager settings) { }

	@Environment(EnvType.CLIENT)
	default public void registerHotkeys(GSKeyManager keyManager) { }

	default public void registerServerSettings(GSSettingManager settings) { }

	default public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) { }

	default public void tick(boolean paused) { }
	
	@Environment(EnvType.CLIENT)
	default public void onJoinServer() { }

	@Environment(EnvType.CLIENT)
	default public void onJoinG4mespeedServer(GSVersion serverVersion) { }

	@Environment(EnvType.CLIENT)
	default public void onDisconnectServer() { }

	default public void onPlayerJoin(ServerPlayerEntity player) { }

	default public void onG4mespeedClientJoin(ServerPlayerEntity player, GSVersion version) { }

	default public void onPlayerLeave(ServerPlayerEntity player) { }

	default public void onPlayerPermissionChanged(ServerPlayerEntity player) { }


}
