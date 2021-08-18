package com.g4mesoft.access.server;

public interface GSIEntityTrackerEntryAccess {

	public boolean isFixedMovement();

	public void setFixedMovement(boolean fixedMovement);

	public void setTickedFromFallingBlock(boolean tickedFromFallingBlock);
	
}
