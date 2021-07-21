package com.g4mesoft.mixin.server;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.GSIChunkHolderAccess;
import com.g4mesoft.access.GSIServerChunkManagerAccess;
import com.g4mesoft.access.GSIThreadedAnvilChunkStorageAccess;

import net.minecraft.entity.Entity;
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
	public void updateBlockImmdiately(BlockPos pos) {
	      int chunkX = pos.getX() >> 4;
	      int chunkZ = pos.getZ() >> 4;
	      ChunkHolder chunkHolder = getChunkHolder(ChunkPos.toLong(chunkX, chunkZ));
	      if (chunkHolder != null)
	    	  ((GSIChunkHolderAccess)chunkHolder).updateBlockImmediately(world, pos);
	}
}
