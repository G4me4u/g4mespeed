package com.g4mesoft.access.common;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.Level;

public interface GSIChunkHolderAccess {

	public void gs_updateBlockImmediately(Level world, BlockPos pos);

	public void gs_updateBlockEntityImmediately(Level world, BlockPos pos);

	/* Schedules a block entity update without a block update. */
	public void gs_markBlockEntityUpdate(BlockPos blockPos);

	public void gs_sendToNearbyPlayers0(Packet<?> packet);

}
