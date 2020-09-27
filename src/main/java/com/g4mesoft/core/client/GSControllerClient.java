package com.g4mesoft.core.client;

import java.io.File;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.GSConnectionPacket;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSIModuleManagerServer;
import com.g4mesoft.gui.GSElementContext;
import com.g4mesoft.gui.GSInfoGUI;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.gui.hotkey.GSHotkeyGUI;
import com.g4mesoft.gui.scroll.GSScrollablePanel;
import com.g4mesoft.gui.setting.GSSettingsGUI;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyBinding;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.setting.GSRemoteSettingManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class GSControllerClient extends GSController implements GSIModuleManagerClient {

	private static final String CLIENT_SETTINGS_GUI_TITLE = "gui.tab.clientSettings";
	private static final String SERVER_SETTINGS_GUI_TITLE = "gui.tab.serverSettings";
	private static final String HOTKEY_GUI_TITLE          = "gui.tab.hotkeys";
	private static final String G4MESPEED_INFO_GUI_TITLE  = "gui.tab.info";
	
	private static final String GS_KEY_CATEGORY = "gs";
	private static final String GUI_KEY_NAME    = "opengui";
	
	private static final String HOTKEY_SETTINGS_FILE_NAME = "hotkeys.cfg";
	
	private static final GSControllerClient instance = new GSControllerClient();
	
	private MinecraftClient minecraft;
	private ClientPlayNetworkHandler networkHandler;

	private final GSExtensionInfoList serverExtensionInfoList;

	private final GSRemoteSettingManager serverSettings;
	private final GSKeyManager keyManager;

	private GSKeyBinding openGUIKey;
	private GSTabbedGUI tabbedGUI;
	
	public GSControllerClient() {
		serverExtensionInfoList = new GSExtensionInfoList();
		
		serverSettings = new GSRemoteSettingManager(this);
		keyManager = new GSKeyManager();
	}

	public void init(MinecraftClient minecraft) {
		this.minecraft = minecraft;

		keyManager.loadKeys(getHotkeySettingsFile());

		openGUIKey = keyManager.registerKey(GUI_KEY_NAME, GS_KEY_CATEGORY, GLFW.GLFW_KEY_G, (key, type) -> {
			// Use lambda to ensure that tabbedGUI has been initialized.
			if (type == GSEKeyEventType.PRESS && tabbedGUI != null)
				GSElementContext.setContent(tabbedGUI);
		}, false);

		GSElementContext.init(minecraft);
		
		tabbedGUI = new GSTabbedGUI();
		tabbedGUI.addTab(CLIENT_SETTINGS_GUI_TITLE, new GSScrollablePanel(new GSSettingsGUI(settings)));
		tabbedGUI.addTab(SERVER_SETTINGS_GUI_TITLE, new GSScrollablePanel(new GSSettingsGUI(serverSettings)));
		tabbedGUI.addTab(HOTKEY_GUI_TITLE,          new GSScrollablePanel(new GSHotkeyGUI(keyManager)));
		tabbedGUI.addTab(G4MESPEED_INFO_GUI_TITLE,  new GSInfoGUI(this));
		
		onStart();
	}
	
	@Override
	public void addModule(GSIModule module) {
		module.registerClientSettings(settings);
		module.registerHotkeys(keyManager);
		
		// Register shadow server settings
		module.registerServerSettings(serverSettings);

		super.addModule(module);
		
		module.initGUI(tabbedGUI);
	}

	public void setNetworkHandler(ClientPlayNetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}
	
	@Override
	public boolean isServerExtensionInstalled(GSExtensionUID extensionUid) {
		return serverExtensionInfoList.isExtensionInstalled(extensionUid);
	}

	@Override
	public boolean isServerExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion) {
		return serverExtensionInfoList.isExtensionInstalled(extensionUid, minimumVersion);
	}

	@Override
	public GSExtensionInfo getServerExtensionInfo(GSExtensionUID extensionUid) {
		return serverExtensionInfoList.getInfo(extensionUid);
	}
	
	@Override
	public GSExtensionInfoList getServerExtensionInfoList() {
		return serverExtensionInfoList;
	}
	
	@Override
	public boolean isG4mespeedServer() {
		return isServerExtensionInstalled(GSCoreExtension.UID);
	}
	
	public void onJoinServer() {
		for (GSIModule module : modules)
			module.onJoinServer();
	}
	
	public void onJoinG4mespeedServer(GSExtensionInfo[] extensionInfo) {
		serverExtensionInfoList.clearInfo();
		serverExtensionInfoList.addAllInfo(extensionInfo);

		if (isServerExtensionInstalled(GSCoreExtension.UID)) {
			sendPacket(new GSConnectionPacket(G4mespeedMod.getExtensionInfoList()));
	
			GSExtensionInfo coreInfo = getServerExtensionInfo(GSCoreExtension.UID);
			
			for (GSIModule module : modules)
				module.onJoinG4mespeedServer(coreInfo);
		}
	}
	
	public void onDisconnectServer() {
		serverExtensionInfoList.clearInfo();

		setNetworkHandler(null);

		for (GSIModule module : modules)
			module.onDisconnectServer();
	
		serverSettings.clearSettings();
	}

	public void onClientClose() {
		keyManager.saveKeys(getHotkeySettingsFile());

		GSElementContext.dispose();
		
		onStop();
	}
	
	private File getHotkeySettingsFile() {
		return new File(getCacheFile(), HOTKEY_SETTINGS_FILE_NAME);
	}

	@Override
	public void tick(boolean paused) {
		super.tick(paused);
		
		keyManager.update();
	}
	
	@Override
	protected void addExtensionModules(GSIExtension extension) {
		extension.addClientModules(this);
	}
	
	@Override
	public boolean isThreadOwner() {
		return minecraft != null && minecraft.isOnThread();
	}
	
	@Override
	public Packet<?> encodeCustomPayload(Identifier identifier, PacketByteBuf buffer) {
		return new CustomPayloadC2SPacket(identifier, buffer);
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public void runOnClient(Consumer<GSIModuleManagerClient> consumer) {
		consumer.accept(this);
	}

	@Override
	public void runOnServer(Consumer<GSIModuleManagerServer> consumer) {
	}
	
	@Override
	public boolean isInGame() {
		return (minecraft != null && minecraft.currentScreen == null);
	}
	
	@Override
	public void sendPacket(GSIPacket packet, GSVersion minExtensionVersion) {
		if (networkHandler != null) {
			GSPacketManager packetManager = G4mespeedMod.getInstance().getPacketManager();
			GSExtensionUID extensionUid = packetManager.getPacketExtensionUniqueId(packet);
			
			if (extensionUid != null && isServerExtensionInstalled(extensionUid, minExtensionVersion)) {
				Packet<?> customPayload = packetManager.encodePacket(packet, this);
				
				if (customPayload != null)
					networkHandler.sendPacket(customPayload);
			}
		}
	}
	
	@Override
	public File getCacheFile() {
		return new File(minecraft.runDirectory, CACHE_DIR_NAME);
	}
	
	public GSKeyManager getKeyManager() {
		return keyManager;
	}
	
	public GSRemoteSettingManager getServerSettings() {
		return serverSettings;
	}
	
	public GSKeyBinding getOpenGUIKey() {
		return openGUIKey;
	}
	
	public static GSControllerClient getInstance() {
		return instance;
	}
}
