package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.network.EntityTrackerEntry;

@Mixin(targets = "net/minecraft/server/world/ServerChunkLoadingManager$EntityTracker")
public interface GSIServerChunkLoadingManagerEntityTrackerAccess {
	
	@Accessor("entry")
	public EntityTrackerEntry getEntry();
	
}
