package com.g4mesoft.access;

import com.g4mesoft.core.GSVersion;

public interface GSINetworkHandlerAccess {

	public void setG4mespeedVersion(GSVersion version);

	public GSVersion getG4mespeedVersion();

	public void setTranslationVersion(byte uid, int translationVersion);

	public int getTranslationVersion(byte uid);
	
}
