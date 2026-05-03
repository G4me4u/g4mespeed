package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.g4mesoft.access.common.GSIChunkHolderAccess;
import com.g4mesoft.access.common.GSIServerChunkCacheAccess;
import com.g4mesoft.access.common.GSIChunkMapAccess;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ServerChunkCache.class)
public abstract class GSServerChunkCacheMixin implements GSIServerChunkCacheAccess {

	@Shadow @Final ServerLevel level;
	
	@Shadow @Final public ChunkMap chunkMap;

	@Shadow protected abstract ChunkHolder getVisibleChunkIfPresent(long chunkId);
	
	@Shadow public abstract void blockChanged(BlockPos pos);
	
	@Override
	public void gs_tickEntityTracker(Entity entity) {
		((GSIChunkMapAccess)chunkMap).gs_tickEntityTracker(entity);
	}
	
	@Override
	public void gs_setTrackerFixedMovement(ServerPlayer player, boolean trackerFixedMovement) {
		((GSIChunkMapAccess)chunkMap).gs_setTrackerFixedMovement(player, trackerFixedMovement);
	}
	
	@Override
	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock) {
		((GSIChunkMapAccess)chunkMap).gs_setTrackerTickedFromFallingBlock(entity, tickedFromFallingBlock);
	}
	
	@Override
	public void gs_flushAndSendChunkUpdates() {
		level.getProfiler().push("chunks");
		if (!level.isDebug()) {
			level.getProfiler().push("pollingChunks");
			// The vanilla implementation actually shuffles the chunks before
			// processing them. This is to ensure that random ticks are being
			// processed randomly. Since we don't process those here, we can
			// broadcast without having to shuffle the chunk holders.
			level.getProfiler().push("broadcast");
			((GSIChunkMapAccess)chunkMap).gs_getChunks().forEach((chunkHolder) -> {
				LevelChunk chunk = chunkHolder.getTickingChunk();
				if (chunk != null)
					chunkHolder.broadcastChanges(chunk);
			});
			level.getProfiler().pop();
			level.getProfiler().pop();
		}
		level.getProfiler().pop();
	}

	@Override
	public void gs_updateBlockImmediately(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_updateBlockImmediately(level, pos);
	}

	@Override
	public void gs_updateBlockEntityImmediately(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_updateBlockEntityImmediately(level, pos);
	}
	
	@Override
	public void gs_markBlockEntityUpdate(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_markBlockEntityUpdate(pos);
	}
	
	@Override
	public void gs_markBlockUpdate(BlockPos pos) {
		blockChanged(pos);
	}
	
	@Override
	public void gs_sendToNearbyPlayers(BlockPos pos, Packet<?> packet) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_sendToNearbyPlayers0(packet);
	}
	
	@Unique
	private ChunkHolder getChunkHolderAt(BlockPos pos) {
		return getVisibleChunkIfPresent(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
	}
}
