package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.common.GSIEntityTrackerEntryAccess;
import com.g4mesoft.access.common.GSIServerChunkLoadingManagerAccess;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;

@Mixin(ServerChunkLoadingManager.class)
public abstract class GSServerChunkLoadingManagerMixin implements GSIServerChunkLoadingManagerAccess {

	@Shadow @Final private Int2ObjectMap<?> entityTrackers;
	
	@Shadow private Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders;
	
	@Override
	public void gs_tickEntityTracker(Entity entity) {
		Object tracker = entityTrackers.get(entity.getId());
		if (tracker != null)
			((GSIServerChunkLoadingManagerEntityTrackerAccess)tracker).getEntry().tick();
	}
	
	@Override
	public void gs_setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement) {
		Object tracker = entityTrackers.get(player.getId());
		if (tracker != null) {
			EntityTrackerEntry entry = ((GSIServerChunkLoadingManagerEntityTrackerAccess)tracker).getEntry();
			((GSIEntityTrackerEntryAccess)entry).gs_setFixedMovement(trackerFixedMovement);
		}
	}

	@Override
	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock) {
		Object tracker = entityTrackers.get(entity.getId());
		if (tracker != null) {
			EntityTrackerEntry entry = ((GSIServerChunkLoadingManagerEntityTrackerAccess)tracker).getEntry();
			((GSIEntityTrackerEntryAccess)entry).gs_setTickedFromFallingBlock(tickedFromFallingBlock);
		}
	}
	
	@Override
	public Iterable<ChunkHolder> gs_getEntryIterator() {
		return chunkHolders.values();
	}
}
