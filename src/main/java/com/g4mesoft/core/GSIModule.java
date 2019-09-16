package com.g4mesoft.core;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIModule {

	public void init(GSIModuleManager manager);
	
	@Environment(EnvType.CLIENT)
	public void keyReleased(int key, int scancode, int mods);

	@Environment(EnvType.CLIENT)
	public void keyPressed(int key, int scancode, int mods);

	@Environment(EnvType.CLIENT)
	public void keyRepeat(int key, int scancode, int mods);

	@Environment(EnvType.CLIENT)
	public void onJoinG4mespeedServer(int serverVersion);

	@Environment(EnvType.CLIENT)
	public void onDisconnectServer();
	
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher);

	public void onPlayerJoin(ServerPlayerEntity player);

	public void onG4mespeedClientJoin(ServerPlayerEntity player, int version);

	public void onPlayerLeave(ServerPlayerEntity player);
	
}
