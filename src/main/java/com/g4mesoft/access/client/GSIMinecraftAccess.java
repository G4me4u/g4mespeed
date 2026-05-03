package com.g4mesoft.access.client;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.dialog.Dialog;

public interface GSIMinecraftAccess {

	public void gs_setFlushingBlockEntityUpdates(boolean flushingUpdates);

	public void gs_schedulePistonMovingBlockEntityUpdate(BlockPos blockPos);
	
	public float gs_getFixedMovementTickDelta();
	
	public Optional<Holder<Dialog>> gs_getQuickActionsDialog();

}
