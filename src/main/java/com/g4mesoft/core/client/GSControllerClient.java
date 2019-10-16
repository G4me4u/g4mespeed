package com.g4mesoft.core.client;

import java.io.File;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.GSVersionPacket;
import com.g4mesoft.core.server.GSIModuleManagerServer;
import com.g4mesoft.gui.GSInfoGUI;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.gui.setting.GSSettingsGUI;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.setting.GSClientSettings;
import com.g4mesoft.setting.GSRemoteSettingManager;

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

	private static final String CLIENT_SETTINGS_GUI_TITLE = "gui.tab.clientSettings";
	private static final String SERVER_SETTINGS_GUI_TITLE = "gui.tab.serverSettings";
	private static final String G4MESPEED_INFO_GUI_TITLE  = "gui.tab.info";
	
	private static final GSControllerClient instance = new GSControllerClient();
	
	private MinecraftClient minecraft;
	private ClientPlayNetworkHandler networkHandler;

	private GSVersion serverVersion;
	private final GSRemoteSettingManager serverSettings;

	private final GSTabbedGUI tabbedGUI;

	private final GSClientSettings clientSettings;
	
	public GSControllerClient() {
		serverVersion = GSVersion.INVALID;
		serverSettings = new GSRemoteSettingManager(this);
		
		tabbedGUI = new GSTabbedGUI();
		tabbedGUI.addTab(CLIENT_SETTINGS_GUI_TITLE, new GSSettingsGUI(settings));
		tabbedGUI.addTab(SERVER_SETTINGS_GUI_TITLE, new GSSettingsGUI(serverSettings));
		tabbedGUI.addTab(G4MESPEED_INFO_GUI_TITLE,  new GSInfoGUI(this));
		
		clientSettings = new GSClientSettings();
	}

	@Override
	public void addModule(GSIModule module) {
		super.addModule(module);
	
		module.initGUI(tabbedGUI);
		module.registerClientSettings(settings);
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

	public void onJoinG4mespeedServer(GSVersion serverVersion) {
		this.serverVersion = serverVersion;
		sendPacket(new GSVersionPacket(getVersion()));

		for (GSIModule module : modules) {
			module.onJoinG4mespeedServer(serverVersion);
			module.registerServerSettings(serverSettings);
		}
	}

	public void onDisconnectServer() {
		this.serverVersion = GSVersion.INVALID;
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
	public GSVersion getVersion() {
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
	public GSVersion getServerVersion() {
		return serverVersion;
	}

	@Override
	public boolean isG4mespeedServer() {
		return !serverVersion.isInvalid();
	}
	
	@Override
	public boolean isInGame() {
		return minecraft != null && minecraft.currentScreen == null;
	}
	
	@Override
	public void sendPacket(GSIPacket packet) {
		sendPacket(packet, GSVersion.MINIMUM_VERSION);
	}

	@Override
	public void sendPacket(GSIPacket packet, GSVersion minimumServerVersion) {
		if (networkHandler == null || serverVersion.isLessThan(minimumServerVersion))
			return;
		
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null)
			networkHandler.sendPacket(customPayload);
	}
	
	@Override
	public File getCacheFile() {
		return new File(minecraft.runDirectory, CACHE_DIR_NAME);
	}
	
	public GSClientSettings getClientSettings() {
		return clientSettings;
	}
	
	public GSRemoteSettingManager getServerSettings() {
		return serverSettings;
	}
	
	public static GSControllerClient getInstance() {
		return instance;
	}
}
