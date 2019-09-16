package com.g4mesoft.core;

import java.util.List;
import java.util.function.Consumer;

import com.g4mesoft.core.client.GSIModuleManagerClient;
import com.g4mesoft.core.server.GSIModuleManagerServer;

public interface GSIModuleManager {

	public List<GSIModule> getModules();

	public void runOnClient(Consumer<GSIModuleManagerClient> consumer);

	public void runOnServer(Consumer<GSIModuleManagerServer> consumer);
	
}
