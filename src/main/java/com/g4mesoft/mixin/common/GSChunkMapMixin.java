package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.common.GSIChunkMapAccess;
import com.g4mesoft.access.common.GSIServerEntityAccess;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(ChunkMap.class)
public abstract class GSChunkMapMixin implements GSIChunkMapAccess {

	@Shadow @Final private Int2ObjectMap<?> entityMap;
	
	@Shadow private Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;
	
	@Override
	public void gs_tickEntityTracker(Entity entity) {
		Object tracker = entityMap.get(entity.getId());
		if (tracker != null)
			((GSIChunkMapTrackedEntityAccess)tracker).getServerEntity().sendChanges();
	}

	@Override
	public void gs_setTrackerFixedMovement(ServerPlayer player, boolean trackerFixedMovement) {
		Object tracker = entityMap.get(player.getId());
		if (tracker != null) {
			ServerEntity entry = ((GSIChunkMapTrackedEntityAccess)tracker).getServerEntity();
			((GSIServerEntityAccess)entry).gs_setFixedMovement(trackerFixedMovement);
		}
	}

	@Override
	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock) {
		Object tracker = entityMap.get(entity.getId());
		if (tracker != null) {
			ServerEntity entry = ((GSIChunkMapTrackedEntityAccess)tracker).getServerEntity();
			((GSIServerEntityAccess)entry).gs_setTickedFromFallingBlock(tickedFromFallingBlock);
		}
	}
	
	@Override
	public Iterable<ChunkHolder> gs_getChunks() {
		return visibleChunkMap.values();
	}
}
