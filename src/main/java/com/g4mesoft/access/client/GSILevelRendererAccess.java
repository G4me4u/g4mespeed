package com.g4mesoft.access.client;

import net.minecraft.core.BlockPos;

public interface GSILevelRendererAccess {

	public void gs_scheduleBlockUpdate(BlockPos pos, boolean important);
	
}
