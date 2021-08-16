package com.g4mesoft.access;

public interface GSIPistonBlockEntityAccess {

	public float getSmoothProgress(float partialTicks);

	public float getOffsetForProgress(float progress, float lastProgress, float partialTicks);
	
}
