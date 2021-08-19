package com.g4mesoft.access.client;

import net.minecraft.util.math.BlockPos;

public interface GSIMinecraftClientAccess {

	public void setFlushingBlockEntityUpdates(boolean flushingUpdates);

	public void schedulePistonBlockEntityUpdate(BlockPos blockPos);
	
	public float getFixedMovementTickDelta();
	
}
