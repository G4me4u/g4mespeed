package com.g4mesoft.access.common;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface GSIServerChunkCacheAccess {

	public void gs_tickEntityTracker(Entity entity);
	
	public void gs_setTrackerFixedMovement(ServerPlayer player, boolean trackerFixedMovement);

	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock);
	
	public void gs_flushAndSendChunkUpdates();
	
	public void gs_updateBlockImmediately(BlockPos pos);

	public void gs_updateBlockEntityImmediately(BlockPos pos);

	public void gs_markBlockEntityUpdate(BlockPos pos);

	public void gs_markBlockUpdate(BlockPos pos);
	
	public void gs_sendToNearbyPlayers(BlockPos pos, Packet<?> packet);

}
