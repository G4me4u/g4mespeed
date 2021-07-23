package com.g4mesoft.access;

import java.util.Set;

import net.minecraft.entity.Entity;

public interface GSIServerPlayerEntity {

	public void onStartTrackingFallingSand(Entity entity);

	public void onStopTrackingFallingSand(Entity entity);

	public Set<Integer> getEntitiesToDestroy();
	
}
