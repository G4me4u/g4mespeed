package com.g4mesoft.access;

import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface GSIChunkHolderAccess {

	public void updateBlockImmediately(World world, BlockPos pos);

	public void updateBlockEntityImmediately(World world, BlockPos pos);

	public void sendToNearbyPlayers0(Packet<?> packet);

}
