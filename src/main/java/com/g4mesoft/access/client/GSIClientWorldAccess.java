package com.g4mesoft.access.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.util.math.BlockPos;

public interface GSIClientWorldAccess {

	public void gs_tickFixedMovementPlayers();
	
	public boolean gs_setBlockStateImmediate(BlockPos pos, BlockState state, int flags);

	public PendingUpdateManager gs_getPendingUpdateManager();
	
}
