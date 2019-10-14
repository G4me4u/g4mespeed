package com.g4mesoft.core.server;

import java.io.File;
import java.util.function.Consumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.GSVersionPacket;
import com.g4mesoft.core.client.GSIModuleManagerClient;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSServerSettingMapPacket;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingChangePacket;
import com.g4mesoft.setting.GSSettingChangePacket.GSESettingChangeType;
import com.g4mesoft.setting.GSSettingMap;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class GSControllerServer extends GSController implements GSIModuleManagerServer, GSISettingChangeListener {

	public static final int OP_PERMISSION_LEVEL = 2;

	private static final GSControllerServer instance = new GSControllerServer();
	
	private CommandDispatcher<ServerCommandSource> dispatcher;
	
	private MinecraftServer server;

	public GSControllerServer() {
		server = null;
		
		settings.addChangeListener(this);
	}
	
	@Override
	public void addModule(GSIModule module) {
		super.addModule(module);
		
		module.registerServerSettings(settings);
		
		if (dispatcher != null)
			module.registerCommands(dispatcher);
	}
	
	public void init(MinecraftServer server) {
		this.server = server;

		onStart();
	}

	public void setCommandDispatcher(CommandDispatcher<ServerCommandSource> dispatcher) {
		GSInfoCommand.registerCommand(dispatcher);
		
		for (GSIModule module : modules)
			module.registerCommands(dispatcher);
		
		this.dispatcher = dispatcher;
	}

	public void onPlayerJoin(ServerPlayerEntity player) {
		sendPacket(new GSVersionPacket(getVersion()), player, GSVersion.INVALID);

		for (GSIModule module : modules)
			module.onPlayerJoin(player);
	}
	
	public void onG4mespeedClientJoined(ServerPlayerEntity player, GSVersion version) {
		((GSINetworkHandlerAccess)player.networkHandler).setG4mespeedVersion(version);

		for (GSIModule module : modules)
			module.onG4mespeedClientJoin(player, version);
		
		for (GSSettingMap settingMap : settings.getSettings())
			sendPacket(new GSServerSettingMapPacket(settingMap), player);
	}

	public void onPlayerLeave(ServerPlayerEntity player) {
		for (GSIModule module : modules)
			module.onPlayerLeave(player);
	}

	public void onServerShutdown() {
		for (GSIModule module : modules)
			module.onServerShutdown();
		
		onStop();
		
		server = null;
	}
	
	@Override
	public boolean isOwnedThread() {
		return server != null && server.isOnThread();
	}

	@Override
	public Packet<?> encodeCustomPayload(Identifier identifier, PacketByteBuf buffer) {
		return new CustomPayloadS2CPacket(identifier, buffer);
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public GSVersion getVersion() {
		return G4mespeedMod.GS_VERSION;
	}

	@Override
	public void runOnClient(Consumer<GSIModuleManagerClient> consumer) {
	}

	@Override
	public void runOnServer(Consumer<GSIModuleManagerServer> consumer) {
		consumer.accept(this);
	}
	
	@Override
	public void sendPacket(GSIPacket packet, ServerPlayerEntity player) {
		sendPacket(packet, player, GSVersion.MINIMUM_VERSION);
	}

	@Override
	public void sendPacket(GSIPacket packet, ServerPlayerEntity player, GSVersion miminumVersion) {
		if (((GSINetworkHandlerAccess)player.networkHandler).getG4mespeedVersion().isLessThan(miminumVersion))
			return;
		
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null)
			player.networkHandler.sendPacket(customPayload);
		
	}

	@Override
	public void sendPacketToAll(GSIPacket packet) {
		sendPacketToAll(packet, GSVersion.MINIMUM_VERSION);
	}

	@Override
	public void sendPacketToAll(GSIPacket packet, GSVersion miminumVersion) {
		if (server == null)
			return;
		
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				GSVersion playerVersion = ((GSINetworkHandlerAccess)player.networkHandler).getG4mespeedVersion();
				
				if (playerVersion.isGreaterThanOrEqualTo(miminumVersion))
					player.networkHandler.sendPacket(customPayload);
			}
		}
	}

	@Override
	public File getCacheFile() {
		if (server.isDedicated())
			return new File(server.getRunDirectory(), CACHE_DIR_NAME);
		
		// Assume we're running on integrated server
		return new File(server.getRunDirectory(), INTEGRATED_CACHE_DIR_NAME);
	}

	@Override
	public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		if (setting.isActive())
			sendPacketToAll(new GSSettingChangePacket(category, setting, GSESettingChangeType.SETTING_CHANGED));
	}

	@Override
	public void onSettingAdded(GSSettingCategory category, GSSetting<?> setting) {
		if (setting.isActive())
			sendPacketToAll(new GSSettingChangePacket(category, setting, GSESettingChangeType.SETTING_ADDED));
	}

	@Override
	public void onSettingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		if (setting.isActive())
			sendPacketToAll(new GSSettingChangePacket(category, setting, GSESettingChangeType.SETTING_REMOVED));
	}
	
	public MinecraftServer getServer() {
		return server;
	}

	public static GSControllerServer getInstance() {
		return instance;
	}
}
