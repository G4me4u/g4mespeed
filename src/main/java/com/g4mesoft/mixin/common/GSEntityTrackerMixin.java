package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.common.GSIEntityTrackerAccess;
import com.g4mesoft.access.common.GSIEntityTrackerEntryAccess;

import net.minecraft.entity.Entity;
import net.minecraft.server.entity.EntityTracker;
import net.minecraft.server.entity.EntityTrackerEntry;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.Int2ObjectHashMap;

@Mixin(EntityTracker.class)
public class GSEntityTrackerMixin implements GSIEntityTrackerAccess {

	@Shadow @Final private Int2ObjectHashMap<EntityTrackerEntry> trackedEntityIds;

	@Override
	public void gs_tickEntityTracker(Entity entity) {
		EntityTrackerEntry entry = trackedEntityIds.get(entity.getNetworkId());
		if (entry != null && entity.world != null)
			entry.notifyNewLocation(entity.world.players);
	}
	
	@Override
	public void gs_setTrackerFixedMovement(ServerPlayerEntity player, boolean trackerFixedMovement) {
		EntityTrackerEntry entry = trackedEntityIds.get(player.getNetworkId());
		if (entry != null)
			((GSIEntityTrackerEntryAccess)entry).gs_setFixedMovement(trackerFixedMovement);
	}

	@Override
	public void gs_setTrackerTickedFromFallingBlock(Entity entity, boolean tickedFromFallingBlock) {
		EntityTrackerEntry entry = trackedEntityIds.get(entity.getNetworkId());
		if (entry != null)
			((GSIEntityTrackerEntryAccess)entry).gs_setTickedFromFallingBlock(tickedFromFallingBlock);
	}
}
