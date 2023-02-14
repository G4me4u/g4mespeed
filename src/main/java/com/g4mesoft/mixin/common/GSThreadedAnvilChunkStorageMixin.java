package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.server.GSIEntityTrackerEntryAccess;
import com.g4mesoft.access.server.GSIThreadedAnvilChunkStorageAccess;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class GSThreadedAnvilChunkStorageMixin implements GSIThreadedAnvilChunkStorageAccess {

	@Shadow @Final private Int2ObjectMap<?> entityTrackers;
	
	@Shadow protected abstract Iterable<ChunkHolder> entryIterator();
	
	@Override
	public void gs_tickEntityTracker(Entity entity) {
		Object tracker = entityTrackers.get(entity.getEntityId());
		if (tracker != null)
			((GSIThreadedAnvilChunkStorageEntityTrackerAccess)tracker).getEntry().tick();
	}
	
	@Override
	public void gs_setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement) {
		Object tracker = entityTrackers.get(player.getEntityId());
		if (tracker != null) {
			EntityTrackerEntry entry = ((GSIThreadedAnvilChunkStorageEntityTrackerAccess)tracker).getEntry();
			((GSIEntityTrackerEntryAccess)entry).gs_setFixedMovement(trackerFixedMovement);
		}
	}

	@Override
	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock) {
		Object tracker = entityTrackers.get(entity.getEntityId());
		if (tracker != null) {
			EntityTrackerEntry entry = ((GSIThreadedAnvilChunkStorageEntityTrackerAccess)tracker).getEntry();
			((GSIEntityTrackerEntryAccess)entry).gs_setTickedFromFallingBlock(tickedFromFallingBlock);
		}
	}
	
	@Override
	public Iterable<ChunkHolder> gs_getEntryIterator() {
		return entryIterator();
	}
}
