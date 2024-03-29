package com.g4mesoft.core.server;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.access.common.GSIServerPlayNetworkHandlerAccess;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSConnectionPacket;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSIClientModuleManager;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSServerSettingMapPacket;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingChangePacket;
import com.g4mesoft.setting.GSSettingManager;
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
import net.minecraft.util.WorldSavePath;

public class GSServerController extends GSController implements GSIServerModuleManager, GSISettingChangeListener {

	public static final int OP_PERMISSION_LEVEL = 2;

	private static final GSServerController instance = new GSServerController();
	
	protected final GSSettingManager worldSettings;

	private CommandDispatcher<ServerCommandSource> dispatcher;
	
	private MinecraftServer server;

	public GSServerController() {
		worldSettings = new GSSettingManager();
		
		server = null;
		
		settings.addChangeListener(this);
		worldSettings.addChangeListener(this);
	}
	
	@Override
	public void addModule(GSIModule module) {
		if (!module.isServerSide())
			throw new IllegalArgumentException("Not a server module.");
		
		module.registerGlobalServerSettings(settings);
		module.registerWorldServerSettings(worldSettings);
		
		if (!worldSettings.isDisjoint(settings))
			throw new IllegalStateException("The global- and world settings are not disjoint!");
		
		if (dispatcher != null)
			module.registerCommands(dispatcher);

		super.addModule(module);
	}
	
	public void init(MinecraftServer server) {
		this.server = server;

		onStart();
	}
	
	@Override
	protected void onStart() {
		worldSettings.loadSettings(getWorldSettingsFile());

		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (!worldSettings.isEmpty())
			worldSettings.saveSettings(getWorldSettingsFile());
		worldSettings.clearSettings();
	}
	
	public void setCommandDispatcher(CommandDispatcher<ServerCommandSource> dispatcher) {
		GSInfoCommand.registerCommand(dispatcher);
		
		for (GSIModule module : modules)
			module.registerCommands(dispatcher);
		
		this.dispatcher = dispatcher;
	}

	public void onPlayerJoin(ServerPlayerEntity player) {
		sendPacket(new GSConnectionPacket(G4mespeedMod.getExtensionInfoList()), player, GSVersion.INVALID);

		for (GSIModule module : modules)
			module.onPlayerJoin(player);
	}
	
	@Override
	public boolean isExtensionInstalled(ServerPlayerEntity player, GSExtensionUID extensionUid) {
		return ((GSIServerPlayNetworkHandlerAccess)player.networkHandler).gs_isExtensionInstalled(extensionUid);
	}
	
	@Override
	public boolean isExtensionInstalled(ServerPlayerEntity player, GSExtensionUID extensionUid, GSVersion minimumVersion) {
		return ((GSIServerPlayNetworkHandlerAccess)player.networkHandler).gs_isExtensionInstalled(extensionUid, minimumVersion);
	}

	@Override
	public GSExtensionInfo getExtensionInfo(ServerPlayerEntity player, GSExtensionUID extensionUid) {
		return ((GSIServerPlayNetworkHandlerAccess)player.networkHandler).gs_getExtensionInfo(extensionUid);
	}
	
	public void onG4mespeedClientJoined(ServerPlayerEntity player, GSExtensionInfo[] extensionInfo) {
		((GSIServerPlayNetworkHandlerAccess)player.networkHandler).gs_clearAllExtensionInfo();
		((GSIServerPlayNetworkHandlerAccess)player.networkHandler).gs_addAllExtensionInfo(extensionInfo);
		
		if (isExtensionInstalled(player, GSCoreExtension.UID)) {
			GSExtensionInfo coreInfo = getExtensionInfo(player, GSCoreExtension.UID);

			for (GSIModule module : modules)
				module.onG4mespeedClientJoin(player, coreInfo);
			
			sendSettings(player);
		}
	}
	
	private void sendSettings(ServerPlayerEntity player) {
		sendSettingPermissionPacket(player);
		
		for (GSSettingMap settingMap : settings.getSettings())
			sendPacket(new GSServerSettingMapPacket(settingMap), player);
		for (GSSettingMap settingMap : worldSettings.getSettings())
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
	public Packet<?> createCustomPayload(Identifier identifier, PacketByteBuf buffer) {
		return new CustomPayloadS2CPacket(identifier, buffer);
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public void runOnClient(Consumer<GSIClientModuleManager> consumer) {
	}

	@Override
	public void runOnServer(Consumer<GSIServerModuleManager> consumer) {
		consumer.accept(this);
	}
	
	@Override
	public void sendPacket(GSIPacket packet, ServerPlayerEntity player, GSVersion minExtensionVersion) {
		if (server != null) {
			GSPacketManager packetManager = G4mespeedMod.getPacketManager();
			GSExtensionUID extensionUid = packetManager.getPacketExtensionUniqueId(packet);
			
			if (extensionUid != null && isExtensionInstalled(player, extensionUid, minExtensionVersion)) {
				Packet<?> customPayload = packetManager.encodePacket(packet, this);
				
				if (customPayload != null)
					player.networkHandler.sendPacket(customPayload);
			}
		}
	}

	@Override
	public void sendPacketToAll(GSIPacket packet, GSVersion minExtensionVersion) {
		sendPacketToAllExcept(packet, minExtensionVersion, null);
	}
	
	@Override
	public void sendPacketToAllExcept(GSIPacket packet, GSVersion minExtensionVersion, ServerPlayerEntity exceptPlayer) {
		if (server != null) {
			GSPacketManager packetManager = G4mespeedMod.getPacketManager();
			GSExtensionUID extensionUid = packetManager.getPacketExtensionUniqueId(packet);
			
			if (extensionUid != null) {
				Packet<?> customPayload = packetManager.encodePacket(packet, this);
	
				if (customPayload != null) {
					for (ServerPlayerEntity player : getAllPlayers()) {
						if (player != exceptPlayer && isExtensionInstalled(player, extensionUid, minExtensionVersion))
							player.networkHandler.sendPacket(customPayload);
					}
				}
			}
		}
	}
	
	@Override
	public ServerPlayerEntity getPlayer(UUID playerUUID) {
		return server.getPlayerManager().getPlayer(playerUUID);
	}
	
	@Override
	public Collection<ServerPlayerEntity> getAllPlayers() {
		return Collections.unmodifiableCollection(server.getPlayerManager().getPlayerList());
	}
	
	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public File getCacheFile() {
		if (server.isDedicated())
			return new File(server.getRunDirectory(), CACHE_DIR_NAME);
		
		// Assume we're running on integrated server
		return new File(server.getRunDirectory(), INTEGRATED_CACHE_DIR_NAME);
	}
	
	@Override
	public GSSettingManager getWorldSettingManager() {
		return worldSettings;
	}
	
	@Override
	public File getWorldCacheFile() {
		return new File(server.getSavePath(WorldSavePath.ROOT).toFile(), CACHE_DIR_NAME);
	}
	
	private File getWorldSettingsFile() {
		return new File(getWorldCacheFile(), SETTINGS_FILE_NAME);
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
		sendPacket(new GSSettingPermissionPacket(isAllowedSettingChange(player)), player);
	}
	
	public static GSServerController getInstance() {
		return instance;
	}
}
