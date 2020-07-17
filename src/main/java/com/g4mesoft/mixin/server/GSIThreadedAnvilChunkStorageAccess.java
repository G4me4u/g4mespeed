package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface GSIThreadedAnvilChunkStorageAccess {

	@Invoker("entryIterator")
	public Iterable<ChunkHolder> invokeEntryIterator();
	
}
