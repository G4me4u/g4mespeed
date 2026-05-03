package com.g4mesoft.access.client;

import net.minecraft.core.BlockPos;

public interface GSIMinecraftAccess {

	public void gs_setFlushingBlockEntityUpdates(boolean flushingUpdates);

	public void gs_schedulePistonMovingBlockEntityUpdate(BlockPos blockPos);
	
	public float gs_getFixedMovementTickDelta();
	
}
