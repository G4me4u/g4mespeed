package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public interface GSIRenderTickCounterAccess {

	@Accessor("tickTime")
	public float getTickTime();
	
}
