package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.World;

@Mixin(World.class)
public interface GSIWorldAccess {

	@Accessor("iteratingTickingBlockEntities")
	public boolean isIteratingTickingBlockEntities();
	
}
