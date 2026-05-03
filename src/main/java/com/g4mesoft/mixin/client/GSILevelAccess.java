package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.Level;

@Mixin(Level.class)
public interface GSILevelAccess {

	@Accessor("tickingBlockEntities")
	public boolean isTickingBlockEntities();
	
}
