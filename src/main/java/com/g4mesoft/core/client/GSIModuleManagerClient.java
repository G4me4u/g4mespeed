package com.g4mesoft.core.client;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.renderer.GSIRenderable3D;

public interface GSIModuleManagerClient extends GSIModuleManager {

	public boolean isServerExtensionInstalled(GSExtensionUID extensionUid);

	public boolean isServerExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion);

	public GSExtensionInfo getServerExtensionInfo(GSExtensionUID extensionUid);
	
	public GSExtensionInfoList getServerExtensionInfoList();
	
	public boolean isG4mespeedServer();

	public boolean isInGame();

	default public void sendPacket(GSIPacket packet) {
		sendPacket(packet, GSVersion.MINIMUM_VERSION);
	}

	public void sendPacket(GSIPacket packet, GSVersion minExtensionVersion);
	
	public void addRenderable(GSIRenderable3D renderable);

	public void removeRenderable(GSIRenderable3D renderable);
	
}
