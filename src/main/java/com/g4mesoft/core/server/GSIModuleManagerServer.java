package com.g4mesoft.core.server;

import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIModuleManagerServer extends GSIModuleManager {

	boolean isExtensionInstalled(ServerPlayerEntity player, byte extensionUid);

	public void sendPacket(GSIPacket packet, ServerPlayerEntity player);

	public void sendPacket(GSIPacket packet, ServerPlayerEntity player, GSVersion miminumVersion);

	public void sendPacketToAll(GSIPacket packet);
	
	public void sendPacketToAll(GSIPacket packet, GSVersion minimumVersion);

}
