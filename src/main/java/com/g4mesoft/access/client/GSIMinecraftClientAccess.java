package com.g4mesoft.access.client;

import net.minecraft.util.math.BlockPos;

public interface GSIMinecraftClientAccess {

	public void gs_setFlushingBlockEntityUpdates(boolean flushingUpdates);

	public void gs_schedulePistonBlockEntityUpdate(BlockPos blockPos);
	
	public float gs_getFixedMovementTickDelta();
	
}
