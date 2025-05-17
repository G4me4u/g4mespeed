package com.g4mesoft.access.client;

public interface GSIMovingBlockEntityAccess {

	public float gs_getOffsetForProgress(float progress, float lastProgress, float partialTicks);
	
	public void gs_onAdded();

	public void gs_handleScheduledUpdate();
	
}
