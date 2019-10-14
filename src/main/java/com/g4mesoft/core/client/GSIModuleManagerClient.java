package com.g4mesoft.core.client;

import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSIPacket;

public interface GSIModuleManagerClient extends GSIModuleManager {

	public GSVersion getServerVersion();

	public boolean isG4mespeedServer();

	public boolean isInGame();

	public void sendPacket(GSIPacket packet);

	public void sendPacket(GSIPacket packet, GSVersion minimumServerVersion);
	
}
