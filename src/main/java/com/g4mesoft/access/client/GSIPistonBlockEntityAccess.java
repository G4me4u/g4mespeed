package com.g4mesoft.access.client;

public interface GSIPistonBlockEntityAccess {

	public float gs_getOffsetForProgress(float progress, float lastProgress, float partialTicks);
	
	public void gs_onAdded();

	public void gs_handleScheduledUpdate();
	
}
