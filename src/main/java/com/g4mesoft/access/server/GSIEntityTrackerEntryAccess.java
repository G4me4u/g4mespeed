package com.g4mesoft.access.server;

public interface GSIEntityTrackerEntryAccess {

	public boolean gs_isFixedMovement();

	public void gs_setFixedMovement(boolean fixedMovement);

	public void gs_setTickedFromFallingBlock(boolean tickedFromFallingBlock);
	
}
