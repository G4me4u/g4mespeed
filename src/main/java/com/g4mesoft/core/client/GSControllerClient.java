package com.g4mesoft.core.client;

import java.io.File;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSVersionPacket;
import com.g4mesoft.core.server.GSIModuleManagerServer;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.gui.setting.GSSettingsGUI;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.setting.GSClientSettings;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingChangePacket;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.GSSettingMap;
import com.g4mesoft.setting.GSSettingChangePacket.GSESettingChangeType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class GSControllerClient extends GSController implements GSIModuleManagerClient {

	private static final String SERVER_SETTINGS_GUI_TITLE = "Server settings";
	private static final String CLIENT_SETTINGS_GUI_TITLE = "Client settings";
	
	private static final GSControllerClient instance = new GSControllerClient();
	
	private MinecraftClient minecraft;
	private ClientPlayNetworkHandler networkHandler;

	private int serverVersion;
	
	private boolean serverChangingSettings;
	private final GSSettingManager serverSettings;

	private final GSTabbedGUI tabbedGUI;

	private final GSClientSettings clientSettings;
	
	public GSControllerClient() {
		serverVersion = G4mespeedMod.INVALID_GS_VERSION;
		serverSettings = new GSSettingManager();
		
		tabbedGUI = new GSTabbedGUI();
		tabbedGUI.addTab(SERVER_SETTINGS_GUI_TITLE, new GSSettingsGUI(serverSettings));
		tabbedGUI.addTab(CLIENT_SETTINGS_GUI_TITLE, new GSSettingsGUI(settings));

		clientSettings = new GSClientSettings();
		
		serverSettings.addChangeListener(new GSISettingChangeListener() {
			@Override
			public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
				if (!serverChangingSettings)
					sendPacket(new GSSettingChangePacket(category, setting, GSESettingChangeType.SETTING_CHANGED));
			}
		});
	}

	@Override
	public void addModule(GSIModule module) {
		super.addModule(module);
	
		module.initGUI(tabbedGUI);
	}
	
	public void init(MinecraftClient minecraft) {
		this.minecraft = minecraft;
		
		onStart();
	}

	public void setNetworkHandler(ClientPlayNetworkHandler networkHandler) {
		this.networkHandler = networkHandler;
	}
	
	public void keyReleased(int key, int scancode, int mods) {
		for (GSIModule module : modules)
			module.keyReleased(key, scancode, mods);
	}

	public void keyPressed(int key, int scancode, int mods) {
		if (key == GLFW.GLFW_KEY_G) {
			minecraft.openScreen(tabbedGUI);
		} else {
			for (GSIModule module : modules)
				module.keyPressed(key, scancode, mods);
		}
	}

	public void keyRepeat(int key, int scancode, int mods) {
		for (GSIModule module : modules)
			module.keyRepeat(key, scancode, mods);
	}

	public void onJoinG4mespeedServer(int serverVersion) {
		this.serverVersion = serverVersion;
		sendPacket(new GSVersionPacket(getVersion()));

		for (GSIModule module : modules)
			module.onJoinG4mespeedServer(serverVersion);
	}

	public void onDisconnectServer() {
		this.serverVersion = G4mespeedMod.INVALID_GS_VERSION;
		setNetworkHandler(null);

		for (GSIModule module : modules)
			module.onDisconnectServer();
	
		serverSettings.clearSettings();
	}

	public void onClientClose() {
		for (GSIModule module : modules)
			module.onClientClose();
		
		onStop();
	}

	public void onServerSettingsReceived(GSSettingMap settingMap) {
		if (!serverSettings.hasCategory(settingMap.getCategory()))
			serverSettings.registerSettingMap(settingMap);
	}
	
	public void onServerSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		GSSetting<?> currentSetting = serverSettings.getSetting(category, setting.getName());
		if (currentSetting != null) {
			serverChangingSettings = true;
			currentSetting.setValueIfSameType(setting);
			serverChangingSettings = false;
		}
	}

	public void onServerSettingAdded(GSSettingCategory category, GSSetting<?> setting) {
		serverChangingSettings = true;
		serverSettings.registerSetting(category, setting);
		serverChangingSettings = false;
	}
	
	public void onServerSettingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		serverChangingSettings = true;
		serverSettings.removeSetting(category, setting.getName());
		serverChangingSettings = false;
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
	public int getVersion() {
		return G4mespeedMod.GS_VERSION;
	}

	@Override
	public void runOnClient(Consumer<GSIModuleManagerClient> consumer) {
		consumer.accept(this);
	}

	@Override
	public void runOnServer(Consumer<GSIModuleManagerServer> consumer) {
	}
	
	@Override
	public int getServerVersion() {
		return serverVersion;
	}

	@Override
	public boolean isG4mespeedServer() {
		return serverVersion != G4mespeedMod.INVALID_GS_VERSION;
	}
	
	@Override
	public boolean isInGame() {
		return minecraft.currentScreen == null;
	}
	
	@Override
	public void sendPacket(GSIPacket packet) {
		sendPacket(packet, true);
	}

	@Override
	public void sendPacket(GSIPacket packet, boolean checkCompatibility) {
		if (checkCompatibility && !isG4mespeedServer())
			return;
		
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null && networkHandler != null)
			networkHandler.sendPacket(customPayload);
	}
	
	@Override
	public File getCacheFile() {
		return new File(minecraft.runDirectory, CACHE_DIR_NAME);
	}
	
	public GSClientSettings getClientSettings() {
		return clientSettings;
	}
	
	public GSSettingManager getServerSettings() {
		return serverSettings;
	}
	
	public static GSControllerClient getInstance() {
		return instance;
	}
}
