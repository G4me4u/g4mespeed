package com.g4mesoft.mixin.server;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.server.GSIChunkHolderAccess;
import com.g4mesoft.access.server.GSIServerChunkManagerAccess;
import com.g4mesoft.access.server.GSIThreadedAnvilChunkStorageAccess;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ServerChunkManager.class)
public abstract class GSServerChunkManagerMixin implements GSIServerChunkManagerAccess {

	@Shadow @Final public ServerWorld world;
	
	@Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

	@Shadow protected abstract ChunkHolder getChunkHolder(long chunkId);
	
	@Override
	public void tickEntityTracker(Entity entity) {
		((GSIThreadedAnvilChunkStorageAccess)threadedAnvilChunkStorage).tickEntityTracker(entity);
	}
	
	@Override
	public void setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement) {
		((GSIThreadedAnvilChunkStorageAccess)threadedAnvilChunkStorage).setTrackerFixedMovement(player, trackerFixedMovement);
	}
	
	@Override
	public void setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock) {
		((GSIThreadedAnvilChunkStorageAccess)threadedAnvilChunkStorage).setTrackerTickedFromFallingBlock(entity, tickedFromFallingBlock);
	}
	
	@Override
	public void flushAndSendChunkUpdates() {
		world.getProfiler().push("chunks");
		
		if (!world.isDebugWorld()) {
			world.getProfiler().push("pollingChunks");
			
			// The vanilla implementation actually shuffles the chunks before
			// processing them. This is to ensure that random ticks are being
			// processed randomly. Since we don't process those here, we can
			// broadcast without having to shuffle the chunk holders.
			((GSIThreadedAnvilChunkStorageAccess)threadedAnvilChunkStorage).getEntryIterator0().forEach((chunkHolder) -> {
				Optional<WorldChunk> optional = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left();
				if (optional.isPresent()) {
					world.getProfiler().push("broadcast");
					chunkHolder.flushUpdates(optional.get());
					world.getProfiler().pop();
				}
			});

			world.getProfiler().pop();
		}
		
		world.getProfiler().pop();
	}

	@Override
	public void updateBlockImmediately(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).updateBlockImmediately(world, pos);
	}

	@Override
	public void updateBlockEntityImmediately(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).updateBlockEntityImmediately(world, pos);
	}
	
	@Override
	public void markBlockEntityUpdate(BlockPos pos) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).markBlockEntityUpdate(pos);
	}
	
	@Override
	public void sendToNearbyPlayers(BlockPos pos, Packet<?> packet) {
		ChunkHolder chunkHolder = getChunkHolderAt(pos);
		if (chunkHolder != null)
			((GSIChunkHolderAccess)chunkHolder).sendToNearbyPlayers0(packet);
	}
	
	private ChunkHolder getChunkHolderAt(BlockPos pos) {
		return getChunkHolder(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4));
	}
}
