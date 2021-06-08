package com.g4mesoft.access;

import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIServerChunkManagerAccess {

	public void tickPlayerTracker(ServerPlayerEntity player);
	
	public void setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);
	
	public void flushAndSendChunkUpdates();
	
}
