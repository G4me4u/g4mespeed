package com.g4mesoft.access;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface GSIServerChunkManagerAccess {

	public void tickEntityTracker(Entity entity);
	
	public void setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);
	
	public void flushAndSendChunkUpdates();
	
	public void updateBlockImmdiately(BlockPos pos);
	
}
