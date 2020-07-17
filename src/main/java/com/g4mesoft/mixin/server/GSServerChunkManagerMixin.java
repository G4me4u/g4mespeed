package com.g4mesoft.mixin.server;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.GSIServerChunkManagerAccess;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.level.LevelGeneratorType;

@Mixin(ServerChunkManager.class)
public class GSServerChunkManagerMixin implements GSIServerChunkManagerAccess {

	@Shadow @Final public ServerWorld world;
	
	@Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

	@Override
	public void flushAndSendChunkUpdates() {
		world.getProfiler().push("chunks");
		
		if (world.getGeneratorType() != LevelGeneratorType.DEBUG_ALL_BLOCK_STATES) {
			world.getProfiler().push("pollingChunks");
			
			((GSIThreadedAnvilChunkStorageAccess)threadedAnvilChunkStorage).invokeEntryIterator().forEach((chunkHolder) -> {
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
}
