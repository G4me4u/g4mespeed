package com.g4mesoft.core.compat;

import com.g4mesoft.core.GSIModuleManager;

public interface GSICarpetTickrateManager {

	public void onInit(GSIModuleManager manager);

	public void onClose();
	
	public boolean isTickrateLinked();
	
	public boolean runsNormally();
	
	public float getTickrate();
	
	public void setTickrate(float tickrate);

	public boolean isPollingCompatMode();
	
	public void addListener(GSICarpetTickrateListener listener);

	public void removeListener(GSICarpetTickrateListener listener);
	
}
