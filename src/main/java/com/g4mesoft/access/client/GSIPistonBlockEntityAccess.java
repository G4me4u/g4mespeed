package com.g4mesoft.access.client;

public interface GSIPistonBlockEntityAccess {

	public float getOffsetForProgress(float progress, float lastProgress, float partialTicks);
	
	public void onAdded();

	public void handleScheduledUpdate();

	public float getProgress();

	public float getNextProgress();

	public void setNextProgress(float nextProgress);
	
}
