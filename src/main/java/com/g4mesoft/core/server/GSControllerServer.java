package com.g4mesoft.core.server;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSExtensionUidsPacket;
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
import com.g4mesoft.setting.GSSettingPermissionPacket;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GSControllerServer extends GSController implements GSIModuleManagerServer, GSISettingChangeListener {

	public static final GSVersion SETTING_PERMISSION_INTRODUCTION = new GSVersion(1, 0, 2);
	
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
		sendPacket(new GSExtensionUidsPacket(G4mespeedMod.getExtensionUids()), player, GSVersion.INVALID);
		sendPacket(new GSVersionPacket(getCoreVersion()), player, GSVersion.INVALID);

		for (GSIModule module : modules)
			module.onPlayerJoin(player);
	}
	
	@Override
	public boolean isExtensionInstalled(ServerPlayerEntity player, GSExtensionUID extensionUid) {
		return ((GSINetworkHandlerAccess)player.networkHandler).isExtensionInstalled(extensionUid);
	}
	
	public void onG4mespeedClientJoined(ServerPlayerEntity player, GSVersion version) {
		((GSINetworkHandlerAccess)player.networkHandler).setCoreVersion(version);

		for (GSIModule module : modules)
			module.onG4mespeedClientJoin(player, version);
		
		sendSettingPermissionPacket(player);

		for (GSSettingMap settingMap : settings.getSettings())
			sendPacket(new GSServerSettingMapPacket(settingMap), player);
	}

	public void onPlayerLeave(ServerPlayerEntity player) {
		for (GSIModule module : modules)
			module.onPlayerLeave(player);
	}

	public void onServerShutdown() {
		onStop();
		
		server = null;
	}
	
	public void onPlayerPermissionChanged(ServerPlayerEntity player) {
		sendSettingPermissionPacket(player);
		
		for (GSIModule module : modules)
			module.onPlayerPermissionChanged(player);
	}
	
	@Override
	protected void addExtensionModules(GSIExtension extension) {
		extension.addServerModules(this);
	}
	
	@Override
	public boolean isThreadOwner() {
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
	public GSVersion getCoreVersion() {
		return G4mespeedMod.GS_CORE_VERSION;
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
		if (((GSINetworkHandlerAccess)player.networkHandler).getCoreVersion().isLessThan(miminumVersion))
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
			for (ServerPlayerEntity player : getAllPlayers()) {
				GSVersion playerVersion = ((GSINetworkHandlerAccess)player.networkHandler).getCoreVersion();
				
				if (playerVersion.isGreaterThanOrEqualTo(miminumVersion))
					player.networkHandler.sendPacket(customPayload);
			}
		}
	}
	
	@Override
	public Collection<ServerPlayerEntity> getAllPlayers() {
		return server.getPlayerManager().getPlayerList();
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
		sendSettingChange(category, setting, GSESettingChangeType.SETTING_CHANGED);
	}

	@Override
	public void onSettingAdded(GSSettingCategory category, GSSetting<?> setting) {
		sendSettingChange(category, setting, GSESettingChangeType.SETTING_ADDED);
	}

	@Override
	public void onSettingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		sendSettingChange(category, setting, GSESettingChangeType.SETTING_REMOVED);
	}
	
	private void sendSettingChange(GSSettingCategory category, GSSetting<?> setting, GSESettingChangeType type) {
		if (!setting.isActive())
			return;

		sendPacketToAll(new GSSettingChangePacket(category, setting, type));
	}
	
	public boolean isAllowedSettingChange(ServerPlayerEntity player) {
		return player.hasPermissionLevel(OP_PERMISSION_LEVEL);
	}
	
	private void sendSettingPermissionPacket(ServerPlayerEntity player) {
		sendPacket(new GSSettingPermissionPacket(isAllowedSettingChange(player)), player, SETTING_PERMISSION_INTRODUCTION);
	}
	
	public MinecraftServer getServer() {
		return server;
	}

	public static GSControllerServer getInstance() {
		return instance;
	}
}
