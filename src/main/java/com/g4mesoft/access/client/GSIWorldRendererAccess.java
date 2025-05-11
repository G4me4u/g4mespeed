package com.g4mesoft.access.client;

import net.minecraft.util.math.BlockPos;

public interface GSIWorldRendererAccess {

	public void gs_scheduleBlockUpdate(BlockPos pos, boolean important);
	
}
