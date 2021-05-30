package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.network.EntityTrackerEntry;

@Mixin(targets = "net/minecraft/server/world/ThreadedAnvilChunkStorage$EntityTracker")
public interface GSIThreadedAnvilChunkStorageEntityTrackerAccess {
	
	@Accessor("entry")
	public EntityTrackerEntry getEntry();
	
}
