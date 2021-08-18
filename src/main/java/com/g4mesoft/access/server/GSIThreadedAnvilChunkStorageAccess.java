package com.g4mesoft.access.server;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;

public interface GSIThreadedAnvilChunkStorageAccess {

	public void tickEntityTracker(Entity entity);
	
	public void setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);

	public void setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock);
	
	public Iterable<ChunkHolder> getEntryIterator0();
	
}
