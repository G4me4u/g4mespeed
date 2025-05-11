package com.g4mesoft.core.server;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.setting.GSSettingManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface GSIServerModule extends GSIModule {

	default public void init(GSIModuleManager manager) {
		if (!(manager instanceof GSIServerModuleManager))
			throw new UnsupportedOperationException();
		init((GSIServerModuleManager)manager);
	}
	
	/**
	 * Invoked during initialization of the <b>server</b>. This method is invoked <i>just
	 * before</i> the main game loop, and might be invoked several times but not before a
	 * matching invocation of {@link #onClose()}.
	 * 
	 * @param manager - the server manager which this module is installed on.
	 */
	public void init(GSIServerModuleManager manager);
	
	@Environment(EnvType.CLIENT)
	default public void initGUI(GSTabbedGUI tabbedGUI) {
		throw new UnsupportedOperationException();
	}

	@Environment(EnvType.CLIENT)
	default public void registerClientSettings(GSSettingManager settings) {
		throw new UnsupportedOperationException();
	}

	@Environment(EnvType.CLIENT)
	default public void registerHotkeys(GSKeyManager keyManager) {
		throw new UnsupportedOperationException();
	}
	
	@Environment(EnvType.CLIENT)
	default public void onJoinServer() {
		throw new UnsupportedOperationException();
	}

	@Environment(EnvType.CLIENT)
	default public void onJoinG4mespeedServer(GSExtensionInfo coreInfo) {
		throw new UnsupportedOperationException();
	}

	@Environment(EnvType.CLIENT)
	default public void onDisconnectServer() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	default public boolean isClientSide() {
		return false;
	}

	@Override
	default public boolean isServerSide() {
		return true;
	}
}
