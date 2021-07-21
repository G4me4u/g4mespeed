package com.g4mesoft.access;

import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIServerWorldAccess {

	public void scheduleDestroyEntityPacket(ServerPlayerEntity player, int entityId);

}
