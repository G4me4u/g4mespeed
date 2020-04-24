package com.g4mesoft.access;

import com.g4mesoft.core.GSVersion;

public interface GSINetworkHandlerAccess {

	public void setCoreVersion(GSVersion version);

	public GSVersion getCoreVersion();

	public boolean isExtensionInstalled(byte extensionUid);

	public void setExtensionUids(byte[] extensionUids);
	
	public byte[] getExtensionUids();
	
	public void setTranslationVersion(byte uid, int translationVersion);

	public int getTranslationVersion(byte uid);
	
}
