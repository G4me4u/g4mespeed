package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.GSIEntityTrackerEntryAccess;
import com.g4mesoft.access.GSIThreadedAnvilChunkStorageAccess;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class GSThreadedAnvilChunkStorageMixin implements GSIThreadedAnvilChunkStorageAccess {

	@Shadow @Final private Int2ObjectMap<?> entityTrackers;
	
	@Shadow protected abstract Iterable<ChunkHolder> entryIterator();
	
	@Override
	public void tickPlayerTracker(ServerPlayerEntity player) {
		Object tracker = entityTrackers.get(player.getEntityId());
		if (tracker != null)
			((GSIThreadedAnvilChunkStorageEntityTrackerAccess)tracker).getEntry().tick();
	}
	
	@Override
	public void setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement) {
		Object tracker = entityTrackers.get(player.getEntityId());
		if (tracker != null) {
			EntityTrackerEntry entry = ((GSIThreadedAnvilChunkStorageEntityTrackerAccess)tracker).getEntry();
			((GSIEntityTrackerEntryAccess)entry).setFixedMovement(trackerFixedMovement);
		}
	}
	
	@Override
	public Iterable<ChunkHolder> getEntryIterator0() {
		return entryIterator();
	}
}
