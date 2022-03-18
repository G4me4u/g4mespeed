package com.g4mesoft.access.server;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.GSVersion;

public interface GSIServerPlayNetworkHandlerAccess {

	public boolean gs_isExtensionInstalled(GSExtensionUID extensionUid);

	public boolean gs_isExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion);

	public GSExtensionInfo gs_getExtensionInfo(GSExtensionUID extensionUid);

	public void gs_clearAllExtensionInfo();
	
	public void gs_addAllExtensionInfo(GSExtensionInfo[] extensionInfo);

	public void gs_addExtensionInfo(GSExtensionInfo extensionInfo);
	
	public void gs_setTranslationVersion(GSExtensionUID uid, int translationVersion);

	public int gs_getTranslationVersion(GSExtensionUID uid);
	
	public boolean gs_isFixedMovement();
	
	public void gs_setFixedMovement(boolean fixedMovement);
	
}
