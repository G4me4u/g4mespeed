package com.g4mesoft.access.server;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface GSIServerChunkManagerAccess {

	public void gs_tickEntityTracker(Entity entity);
	
	public void gs_setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);

	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock);
	
	public void gs_flushAndSendChunkUpdates();
	
	public void gs_updateBlockImmediately(BlockPos pos);

	public void gs_updateBlockEntityImmediately(BlockPos pos);

	public void gs_markBlockEntityUpdate(BlockPos pos);

	public void gs_sendToNearbyPlayers(BlockPos pos, Packet<?> packet);

}
