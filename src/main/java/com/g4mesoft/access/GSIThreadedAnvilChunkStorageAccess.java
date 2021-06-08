package com.g4mesoft.access;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;

public interface GSIThreadedAnvilChunkStorageAccess {

	public void tickPlayerTracker(ServerPlayerEntity player);
	
	public void setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);
	
	public Iterable<ChunkHolder> getEntryIterator0();
	
}
