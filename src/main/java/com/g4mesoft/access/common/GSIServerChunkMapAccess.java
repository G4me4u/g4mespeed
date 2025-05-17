package com.g4mesoft.access.common;

import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;

public interface GSIServerChunkMapAccess {

	public void gs_flushAndSendChunkUpdates();
	
	public void gs_updateBlockImmediately(BlockPos pos);

	public void gs_updateBlockEntityImmediately(BlockPos pos);

	public void gs_markBlockEntityUpdate(BlockPos pos);

	public void gs_markBlockUpdate(BlockPos pos);
	
	public void gs_sendToNearbyPlayers(BlockPos pos, Packet<?> packet);

}
