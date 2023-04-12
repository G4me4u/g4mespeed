package com.g4mesoft.access.common;

import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface GSIChunkHolderAccess {

	public void gs_updateBlockImmediately(World world, BlockPos pos);

	public void gs_updateBlockEntityImmediately(World world, BlockPos pos);

	/* Schedules a block entity update without a block update. */
	public void gs_markBlockEntityUpdate(BlockPos blockPos);

	public void gs_sendToNearbyPlayers0(Packet<?> packet);

}
