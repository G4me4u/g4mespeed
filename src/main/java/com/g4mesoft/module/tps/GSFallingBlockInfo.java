package com.g4mesoft.module.tps;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class GSFallingBlockInfo {

	private final ServerPlayer player;
	private final BlockPos blockPos;
	private final int entityId;
	
	public GSFallingBlockInfo(ServerPlayer player, BlockPos blockPos, int entityId) {
		this.player = player;
		this.blockPos = blockPos;
		this.entityId = entityId;
	}

	public ServerPlayer getPlayer() {
		return player;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public int getEntityId() {
		return entityId;
	}
}
