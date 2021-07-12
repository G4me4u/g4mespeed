package com.g4mesoft.core;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import com.g4mesoft.core.client.GSIClientModuleManager;
import com.g4mesoft.core.server.GSIServerModuleManager;
import com.g4mesoft.setting.GSSettingManager;

public interface GSIModuleManager {

	public void addModule(GSIModule module);
	
	public List<GSIModule> getModules();

	public void runOnClient(Consumer<GSIClientModuleManager> consumer);

	public void runOnServer(Consumer<GSIServerModuleManager> consumer);
	
	public GSSettingManager getSettingManager();

	public File getCacheFile();
	
}
