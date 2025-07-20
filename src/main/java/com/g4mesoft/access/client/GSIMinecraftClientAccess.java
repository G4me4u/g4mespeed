package com.g4mesoft.access.client;

import java.util.Optional;

import net.minecraft.dialog.type.Dialog;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;

public interface GSIMinecraftClientAccess {

	public void gs_setFlushingBlockEntityUpdates(boolean flushingUpdates);

	public void gs_schedulePistonBlockEntityUpdate(BlockPos blockPos);
	
	public float gs_getFixedMovementTickDelta();
	
	public Optional<RegistryEntry<Dialog>> gs_getQuickActionsDialog();

}
