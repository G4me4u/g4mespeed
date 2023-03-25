package com.g4mesoft.access.client;

import net.minecraft.client.network.PendingUpdateManager;

public interface GSIClientWorldAccess {

	public void gs_tickFixedMovementPlayers();
	
	public PendingUpdateManager gs_getPendingUpdateManager();
	
}
