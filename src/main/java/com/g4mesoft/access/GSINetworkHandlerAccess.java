package com.g4mesoft.access;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.GSVersion;

public interface GSINetworkHandlerAccess {

	public boolean isExtensionInstalled(GSExtensionUID extensionUid);

	public boolean isExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion);

	public GSExtensionInfo getExtensionInfo(GSExtensionUID extensionUid);

	public void clearAllExtensionInfo();
	
	public void addAllExtensionInfo(GSExtensionInfo[] extensionInfo);

	public void addExtensionInfo(GSExtensionInfo extensionInfo);
	
	public void setTranslationVersion(GSExtensionUID uid, int translationVersion);

	public int getTranslationVersion(GSExtensionUID uid);
	
}
