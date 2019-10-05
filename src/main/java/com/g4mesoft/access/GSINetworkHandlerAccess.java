package com.g4mesoft.access;

public interface GSINetworkHandlerAccess {

	public void setG4mespeedInstalled(boolean gsInstalled);

	public boolean isG4mespeedInstalled();

	public void setG4mespeedVersion(int gsVersion);

	public int getG4mespeedVersion();

	public void setTranslationVersion(int translationVersion);

	public int getTranslationVersion();
	
}
