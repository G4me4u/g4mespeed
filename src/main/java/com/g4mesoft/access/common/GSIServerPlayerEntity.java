package com.g4mesoft.access.common;

import java.util.Set;

import net.minecraft.entity.Entity;

public interface GSIServerPlayerEntity {

	public void gs_onStartTrackingFallingSand(Entity entity);

	public void gs_onStopTrackingFallingSand(Entity entity);

	public Set<Integer> gs_getEntitiesToDestroy();
	
}
