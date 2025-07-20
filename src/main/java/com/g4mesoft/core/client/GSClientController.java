package com.g4mesoft.core.client;

import java.io.File;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.access.client.GSIMinecraftClientAccess;
import com.g4mesoft.core.GSConnectionPacket;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSIServerModuleManager;
import com.g4mesoft.gui.GSContentHistoryGUI;
import com.g4mesoft.gui.GSHotkeyGUI;
import com.g4mesoft.gui.GSInfoGUI;
import com.g4mesoft.gui.GSKeyBindingButtonStroke;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.gui.setting.GSSettingsGUI;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyBinding;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.mixin.client.GSIKeyBindingAccess;
import com.g4mesoft.packet.GSCustomPayload;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.setting.GSRemoteSettingManager;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.ui.G4mespeedUIMod;
import com.g4mesoft.ui.panel.GSPanelContext;
import com.g4mesoft.ui.panel.dialog.GSConfirmDialog;
import com.g4mesoft.ui.panel.dialog.GSConfirmOption;
import com.g4mesoft.ui.panel.dialog.GSEConfirmOptionPlacement;
import com.g4mesoft.ui.panel.scroll.GSScrollPanel;
import com.g4mesoft.ui.renderer.GSIRenderable3D;
import com.g4mesoft.ui.util.GSTextUtil;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class GSClientController extends GSController implements GSIClientModuleManager {

	public static final String CLIENT_SETTINGS_GUI_TITLE = "gui.tab.clientSettings";
	private static final String SERVER_SETTINGS_GUI_TITLE = "gui.tab.serverSettings";
	public static final String HOTKEY_GUI_TITLE          = "gui.tab.hotkeys";
	public static final String G4MESPEED_INFO_GUI_TITLE  = "gui.tab.info";
	
	private static final String GS_KEY_CATEGORY = "gs";
	private static final String GUI_KEY_NAME    = "opengui";
	
	private static final String HOTKEY_SETTINGS_FILE_NAME = "hotkeys.cfg";
	
	private static final Text QUICK_ACTIONS_OVERRIDE_NOTICE = GSTextUtil.translatable("panel.confirmDialog.quickActionsOverrideNotice");
	private static final GSConfirmOption QUICK_ACTION_OVERRIDE_OK_OPTION = new GSConfirmOption(
		GSTextUtil.translatable("panel.confirmDialog.ok"),
		GSEConfirmOptionPlacement.RIGHT
	);
	private static final GSConfirmOption QUICK_ACTION_OVERRIDE_DONT_SHOW_AGAIN_OPTION = new GSConfirmOption(
		GSTextUtil.translatable("panel.confirmDialog.dontShowAgain"),
		GSEConfirmOptionPlacement.RIGHT
	);
	private static final GSConfirmOption[] QUICK_ACTION_OVERRIDE_OPTIONS = new GSConfirmOption[] {
		QUICK_ACTION_OVERRIDE_OK_OPTION,
		QUICK_ACTION_OVERRIDE_DONT_SHOW_AGAIN_OPTION
	};

	public static final GSSettingCategory GENERAL_CATEGORY = new GSSettingCategory("general");

	private static final GSClientController instance = new GSClientController();
	
	private MinecraftClient minecraft;
	private boolean connectedToServer;
	private ClientPlayNetworkHandler networkHandler;

	private final GSExtensionInfoList serverExtensionInfoList;

	private final GSRemoteSettingManager serverSettings;
	private final GSKeyManager keyManager;

	private GSKeyBinding openGUIKey;
	private GSTabbedGUI tabbedGUI;
	private GSContentHistoryGUI contentHistoryGUI;
	
	private final GSBooleanSetting cQuickActionsOverrideNotice;
	
	public GSClientController() {
		serverExtensionInfoList = new GSExtensionInfoList();
		
		serverSettings = new GSRemoteSettingManager(this);
		keyManager = new GSKeyManager();
		
		cQuickActionsOverrideNotice = new GSBooleanSetting("quickActionsOverrideNotice", true);
	}

	public void init(MinecraftClient minecraft) {
		if (this.minecraft == null) {
			this.minecraft = minecraft;

			keyManager.loadKeys(getHotkeySettingsFile());
	
			openGUIKey = keyManager.registerKey(GUI_KEY_NAME, GS_KEY_CATEGORY, GLFW.GLFW_KEY_G, () -> {
				// Use lambda to ensure that contentHistoryGUI has been initialized.
				if (contentHistoryGUI != null) {
					GSPanelContext.openContent(contentHistoryGUI);
					
					if (isQuickActionsKeybindOverride() && cQuickActionsOverrideNotice.get()) {
						GSConfirmDialog dialog = GSConfirmDialog.showDialog(contentHistoryGUI,
								QUICK_ACTIONS_OVERRIDE_NOTICE, QUICK_ACTION_OVERRIDE_OPTIONS);
						dialog.addActionListener(() -> {
							if (dialog.hasSelection() && dialog.getSelectedOption() == QUICK_ACTION_OVERRIDE_DONT_SHOW_AGAIN_OPTION)
								cQuickActionsOverrideNotice.set(false);
						});
					}
				}
			}, GSEKeyEventType.PRESS, false);
	
			tabbedGUI = new GSTabbedGUI();
			tabbedGUI.addTab(CLIENT_SETTINGS_GUI_TITLE, new GSScrollPanel(new GSSettingsGUI(settings)));
			tabbedGUI.addTab(SERVER_SETTINGS_GUI_TITLE, new GSScrollPanel(new GSSettingsGUI(serverSettings)));
			tabbedGUI.addTab(HOTKEY_GUI_TITLE,          new GSScrollPanel(new GSHotkeyGUI(keyManager)));
			tabbedGUI.addTab(G4MESPEED_INFO_GUI_TITLE,  new GSInfoGUI(this));
			
			contentHistoryGUI = new GSContentHistoryGUI(tabbedGUI, new GSKeyBindingButtonStroke(openGUIKey));
			
			// Register setting for showing quick actions override notice.
			settings.registerSetting(GENERAL_CATEGORY, cQuickActionsOverrideNotice);
			
			onStart();
		}
	}
	
	public boolean isQuickActionsKeybindOverride() {
		if (((GSIMinecraftClientAccess)minecraft).gs_getQuickActionsDialog().isEmpty()) {
			// Notice is not required (there is no quick actions dialog).
			return false;
		}
		
		// Override quick actions when there is overlap with our hotkey.
		InputUtil.Key key = ((GSIKeyBindingAccess)minecraft.options.quickActionsKey).gs_getBoundKey();
		return openGUIKey.getKeyCode().indexOf(key) != -1;
	}
	
	@Override
	public void addModule(GSIModule module) {
		if (!module.isClientSide())
			throw new IllegalArgumentException("Not a client module.");
		
		module.registerClientSettings(settings);
		module.registerHotkeys(keyManager);
		
		// Register shadow server settings
		module.registerGlobalServerSettings(serverSettings);
		module.registerWorldServerSettings(serverSettings);

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
	
	public boolean isConnectedToServer() {
		return connectedToServer;
	}
	
	public void onJoinServer() {
		connectedToServer = true;
		
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
		connectedToServer = false;
		
		setNetworkHandler(null);

		for (GSIModule module : modules)
			module.onDisconnectServer();

		contentHistoryGUI.clearHistory();
	
		serverExtensionInfoList.clearInfo();
		serverSettings.clearSettings();
	}

	public void onClientClose() {
		if (minecraft != null) {
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
	protected void addExtensionModules(GSIExtension extension) {
		extension.addClientModules(this);
	}
	
	@Override
	public boolean isThreadOwner() {
		return minecraft != null && minecraft.isOnThread();
	}
	
	@Override
	public Packet<?> createCustomPayload(ByteBuf buffer) {
		return new CustomPayloadC2SPacket(GSCustomPayload.create(buffer));
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public void runOnClient(Consumer<GSIClientModuleManager> consumer) {
		consumer.accept(this);
	}

	@Override
	public void runOnServer(Consumer<GSIServerModuleManager> consumer) {
	}
	
	@Override
	public boolean isInGame() {
		return (minecraft != null && minecraft.currentScreen == null);
	}
	
	@Override
	public void sendPacket(GSIPacket packet, GSVersion minExtensionVersion) {
		if (networkHandler != null) {
			GSPacketManager packetManager = G4mespeedMod.getPacketManager();
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
		G4mespeedUIMod.addRenderable(renderable);
	}

	@Override
	public void removeRenderable(GSIRenderable3D renderable) {
		G4mespeedUIMod.removeRenderable(renderable);
	}
	
	public ClientPlayerEntity getPlayer() {
		return (minecraft != null) ? minecraft.player : null;
	}
	
	public MinecraftClient getClient() {
		return minecraft;
	}
	
	public GSKeyManager getKeyManager() {
		return keyManager;
	}
	
	public GSRemoteSettingManager getServerSettings() {
		return serverSettings;
	}
	
	public GSTabbedGUI getTabbedGUI() {
		return tabbedGUI;
	}
	
	public GSContentHistoryGUI getPrimaryGUI() {
		return contentHistoryGUI;
	}
	
	public GSKeyBinding getOpenGUIKey() {
		return openGUIKey;
	}

	public static GSClientController getInstance() {
		return instance;
	}
}
