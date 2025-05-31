package com.g4mesoft.access.client;

import java.util.function.Consumer;

import net.minecraft.entity.Entity;

public interface GSIClientWorldAccess {

	public void gs_forEachEntity(Consumer<Entity> action);
	
	public void gs_tickFixedMovementPlayers();
	
}
