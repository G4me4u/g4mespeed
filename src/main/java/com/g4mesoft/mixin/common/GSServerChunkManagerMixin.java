package com.g4mesoft.mixin.common;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.g4mesoft.access.common.GSIChunkHolderAccess;
import com.g4mesoft.access.common.GSIServerChunkMapAccess;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.ChunkHolder;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ChunkMap.class)
public abstract class GSServerChunkManagerMixin implements GSIServerChunkMapAccess {

	@Shadow @Final public ServerWorld world;
	@Shadow @Final private Set<ChunkHolder> dirty;
	
	@Shadow protected abstract ChunkHolder getChunk(int chunkX, int chunkZ);
	
	@Shadow public abstract void onBlockChanged(BlockPos pos);
	
	@Override
	public void gs_flushAndSendChunkUpdates() {
		if (!dirty.isEmpty()) {
			for (ChunkHolder chunkHolder : dirty)
				chunkHolder.sendChanges();
			dirty.clear();
		}
	}

	@Override
	public void gs_updateBlockImmediately(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_updateBlockImmediately(world, pos);
	}

	@Override
	public void gs_updateBlockEntityImmediately(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_updateBlockEntityImmediately(world, pos);
	}
	
	@Override
	public void gs_markBlockEntityUpdate(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_markBlockEntityUpdate(pos);
	}
	
	@Override
	public void gs_markBlockUpdate(BlockPos pos) {
		onBlockChanged(pos);
	}
	
	@Override
	public void gs_sendToNearbyPlayers(BlockPos pos, Packet<?> packet) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_sendToNearbyPlayers0(packet);
	}
	
	@Unique
	private ChunkHolder getChunkHolderAt(BlockPos pos) {
		return getChunk(pos.getX() >> 4, pos.getZ() >> 4);
	}
}
