package com.g4mesoft.access;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface GSIServerChunkManagerAccess {

	public void tickEntityTracker(Entity entity);
	
	public void setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);

	public void setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock);
	
	public void flushAndSendChunkUpdates();
	
	public void updateBlockImmediately(BlockPos pos);

	public void updateBlockEntityImmediately(BlockPos pos);

	public void sendToNearbyPlayers(BlockPos pos, Packet<?> packet);

}
