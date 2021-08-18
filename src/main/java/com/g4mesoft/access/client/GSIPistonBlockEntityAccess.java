package com.g4mesoft.access.client;

public interface GSIPistonBlockEntityAccess {

	public float getSmoothProgress(float partialTicks);

	public float getOffsetForProgress(float progress, float lastProgress, float partialTicks);
	
	public void onAdded();

}