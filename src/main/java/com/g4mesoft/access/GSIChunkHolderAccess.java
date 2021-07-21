package com.g4mesoft.access;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface GSIChunkHolderAccess {

	public void updateBlockImmediately(World world, BlockPos pos);

}
