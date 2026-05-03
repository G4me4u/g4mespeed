package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerEntity;

@Mixin(targets = "net/minecraft/server/level/ChunkMap$TrackedEntity")
public interface GSIChunkMapTrackedEntityAccess {
	
	@Accessor("serverEntity")
	public ServerEntity getServerEntity();
	
}
