package com.g4mesoft.core.client;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import com.g4mesoft.gui.GSHotkeyGUI;
import com.g4mesoft.gui.GSInfoGUI;
import com.g4mesoft.gui.GSSettingsGUI;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyBinding;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.renderer.GSERenderPhase;
import com.g4mesoft.renderer.GSIRenderable3D;
import com.g4mesoft.renderer.GSIRenderer3D;
import com.g4mesoft.setting.GSRemoteSettingManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

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
	
	private List<GSIRenderable3D> renderables;
	
	public GSControllerClient() {
		serverExtensionInfoList = new GSExtensionInfoList();
		
		serverSettings = new GSRemoteSettingManager(this);
		keyManager = new GSKeyManager();
		
		renderables = new LinkedList<>();
	}

	public void init(MinecraftClient minecraft) {
		if (this.minecraft == null) {
			this.minecraft = minecraft;

			keyManager.loadKeys(getHotkeySettingsFile());
	
			openGUIKey = keyManager.registerKey(GUI_KEY_NAME, GS_KEY_CATEGORY, GLFW.GLFW_KEY_G, (key, type) -> {
				// Use lambda to ensure that tabbedGUI has been initialized.
				if (type == GSEKeyEventType.PRESS && tabbedGUI != null)
					GSPanelContext.setContent(tabbedGUI);
			}, false);
	
			GSPanelContext.init(minecraft);
			
			tabbedGUI = new GSTabbedGUI();
			tabbedGUI.addTab(CLIENT_SETTINGS_GUI_TITLE, new GSScrollPanel(new GSSettingsGUI(settings)));
			tabbedGUI.addTab(SERVER_SETTINGS_GUI_TITLE, new GSScrollPanel(new GSSettingsGUI(serverSettings)));
			tabbedGUI.addTab(HOTKEY_GUI_TITLE,          new GSScrollPanel(new GSHotkeyGUI(keyManager)));
			tabbedGUI.addTab(G4MESPEED_INFO_GUI_TITLE,  new GSInfoGUI(this));
			
			addRenderable(new GSIRenderable3D() {
				
				@Override
				public void render(GSIRenderer3D renderer3d) {
					Vec3d cameraPos = minecraft.gameRenderer.getCamera().getPos();
					renderer3d.fillCube(-(float)cameraPos.getX(), -(float)cameraPos.getY(), -(float)cameraPos.getZ(), 0x80FF00FF);
				}
				
				@Override
				public GSERenderPhase getRenderPhase() {
					return GSERenderPhase.TRANSPARENT_LAST;
				}
			});
			
			onStart();
		}
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
		setNetworkHandler(null);

		for (GSIModule module : modules)
			module.onDisconnectServer();
	
		serverExtensionInfoList.clearInfo();
		serverSettings.clearSettings();
	}

	public void onClientClose() {
		if (minecraft != null) {
			GSPanelContext.dispose();

			keyManager.saveKeys(getHotkeySettingsFile());
			keyManager.dispose();

			onStop();
			
			openGUIKey = null;
			tabbedGUI = null;
			minecraft = null;
		}
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
	
	@Override
	public void addRenderable(GSIRenderable3D renderable) {
		renderables.add(renderable);
	}

	@Override
	public void removeRenderable(GSIRenderable3D renderable) {
		renderables.remove(renderable);
	}
	
	public Collection<GSIRenderable3D> getRenderables() {
		return Collections.unmodifiableCollection(renderables);
	}
	
	public ClientPlayerEntity getPlayer() {
		return (minecraft != null) ? minecraft.player : null;
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
