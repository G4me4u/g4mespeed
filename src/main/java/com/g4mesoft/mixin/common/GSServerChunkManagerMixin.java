package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.g4mesoft.access.common.GSIChunkHolderAccess;
import com.g4mesoft.access.common.GSIServerChunkManagerAccess;
import com.g4mesoft.access.common.GSIServerChunkLoadingManagerAccess;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ServerChunkManager.class)
public abstract class GSServerChunkManagerMixin implements GSIServerChunkManagerAccess {

	@Shadow @Final public ServerWorld world;
	
	@Shadow @Final public ServerChunkLoadingManager chunkLoadingManager;

	@Shadow protected abstract ChunkHolder getChunkHolder(long chunkId);
	
	@Shadow public abstract void markForUpdate(BlockPos pos);
	
	@Override
	public void gs_tickEntityTracker(Entity entity) {
		((GSIServerChunkLoadingManagerAccess)chunkLoadingManager).gs_tickEntityTracker(entity);
	}
	
	@Override
	public void gs_setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement) {
		((GSIServerChunkLoadingManagerAccess)chunkLoadingManager).gs_setTrackerFixedMovement(player, trackerFixedMovement);
	}
	
	@Override
	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock) {
		((GSIServerChunkLoadingManagerAccess)chunkLoadingManager).gs_setTrackerTickedFromFallingBlock(entity, tickedFromFallingBlock);
	}
	
	@Override
	public void gs_flushAndSendChunkUpdates() {
		world.getProfiler().push("chunks");
		if (!world.isDebugWorld()) {
			world.getProfiler().push("pollingChunks");
			// The vanilla implementation actually shuffles the chunks before
			// processing them. This is to ensure that random ticks are being
			// processed randomly. Since we don't process those here, we can
			// broadcast without having to shuffle the chunk holders.
			world.getProfiler().push("broadcast");
			((GSIServerChunkLoadingManagerAccess)chunkLoadingManager).gs_getEntryIterator().forEach((chunkHolder) -> {
				WorldChunk chunk = chunkHolder.getWorldChunk();
				if (chunk != null)
					chunkHolder.flushUpdates(chunk);
			});
			world.getProfiler().pop();
			world.getProfiler().pop();
		}
		world.getProfiler().pop();
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
		markForUpdate(pos);
	}
	
	@Override
	public void gs_sendToNearbyPlayers(BlockPos pos, Packet<?> packet) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).gs_sendToNearbyPlayers0(packet);
	}
	
	@Unique
	private ChunkHolder getChunkHolderAt(BlockPos pos) {
		return getChunkHolder(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4));
	}
}
