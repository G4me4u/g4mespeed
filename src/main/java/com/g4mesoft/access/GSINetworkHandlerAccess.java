package com.g4mesoft.access;

import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.GSVersion;

public interface GSINetworkHandlerAccess {

	public void setCoreVersion(GSVersion version);

	public GSVersion getCoreVersion();

	public boolean isExtensionInstalled(GSExtensionUID extensionUid);

	public void setExtensionUids(GSExtensionUID[] extensionUids);
	
	public GSExtensionUID[] getExtensionUids();
	
	public void setTranslationVersion(GSExtensionUID uid, int translationVersion);

	public int getTranslationVersion(GSExtensionUID uid);
	
}
