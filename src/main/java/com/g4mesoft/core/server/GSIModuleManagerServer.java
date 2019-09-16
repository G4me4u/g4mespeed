package com.g4mesoft.core.server;

import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIModuleManagerServer extends GSIModuleManager {

	public void sendPacket(GSIPacket packet, ServerPlayerEntity player);

	public void sendPacket(GSIPacket packet, ServerPlayerEntity player, boolean checkCompatibility);

	public void sendPacketToAll(GSIPacket packet);

	public void sendPacketToAll(GSIPacket packet, boolean checkCompatibility);

}
