package com.g4mesoft.access.common;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;

public interface GSIThreadedAnvilChunkStorageAccess {

	public void gs_tickEntityTracker(Entity entity);
	
	public void gs_setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);

	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock);
	
	public Iterable<ChunkHolder> gs_getEntryIterator();
	
}
