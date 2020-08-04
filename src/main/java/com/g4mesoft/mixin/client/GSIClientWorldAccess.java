package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(ClientWorld.class)
public interface GSIClientWorldAccess {

	@Invoker("checkChunk")
	public void invokeCheckChunk(Entity entity);
	
}
