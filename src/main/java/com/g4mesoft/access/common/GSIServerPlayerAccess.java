package com.g4mesoft.access.common;

import java.util.Set;

import net.minecraft.world.entity.Entity;

public interface GSIServerPlayerAccess {

	public void gs_onStartTrackingFallingSand(Entity entity);

	public void gs_onStopTrackingFallingSand(Entity entity);

	public Set<Integer> gs_getEntitiesToDestroy();
	
}
