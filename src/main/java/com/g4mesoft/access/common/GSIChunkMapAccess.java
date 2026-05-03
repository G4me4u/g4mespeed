package com.g4mesoft.access.common;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface GSIChunkMapAccess {

	public void gs_tickEntityTracker(Entity entity);
	
	public void gs_setTrackerFixedMovement(ServerPlayer player, boolean trackerFixedMovement);

	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock);
	
	public Iterable<ChunkHolder> gs_getChunks();
	
}
