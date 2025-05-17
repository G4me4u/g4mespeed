package com.g4mesoft.access.common;

import net.minecraft.entity.Entity;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

public interface GSIEntityTrackerAccess {

	public void gs_tickEntityTracker(Entity entity);
	
	public void gs_setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement);

	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock);

}
