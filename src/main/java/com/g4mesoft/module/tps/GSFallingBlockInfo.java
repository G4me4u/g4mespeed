package com.g4mesoft.module.tps;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class GSFallingBlockInfo {

	private final ServerPlayerEntity player;
	private final BlockPos blockPos;
	private final int entityId;
	
	public GSFallingBlockInfo(ServerPlayerEntity player, BlockPos blockPos, int entityId) {
		this.player = player;
		this.blockPos = blockPos;
		this.entityId = entityId;
	}

	public ServerPlayerEntity getPlayer() {
		return player;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public int getEntityId() {
		return entityId;
	}
}
