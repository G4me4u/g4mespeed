package com.g4mesoft.access.client;

import java.util.function.Consumer;

import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface GSIClientLevelAccess {

	public void gs_forEachEntity(Consumer<Entity> action);
	
	public void gs_tickFixedMovementPlayers();
	
	public boolean gs_setBlockStateImmediate(BlockPos pos, BlockState state, int flags);

	public BlockStatePredictionHandler gs_getPendingUpdateManager();
	
}
