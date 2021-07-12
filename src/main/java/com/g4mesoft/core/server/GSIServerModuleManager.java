package com.g4mesoft.core.server;

import java.util.Collection;
import java.util.UUID;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIServerModuleManager extends GSIModuleManager {

	public boolean isExtensionInstalled(ServerPlayerEntity player, GSExtensionUID extensionUid);

	public boolean isExtensionInstalled(ServerPlayerEntity player, GSExtensionUID extensionUid, GSVersion minimumVersion);
	
	public GSExtensionInfo getExtensionInfo(ServerPlayerEntity player, GSExtensionUID extensionUid);
	
	default public void sendPacket(GSIPacket packet, ServerPlayerEntity player) {
		sendPacket(packet, player, GSVersion.MINIMUM_VERSION);
	}

	public void sendPacket(GSIPacket packet, ServerPlayerEntity player, GSVersion minExtensionVersion);

	default public void sendPacketToAll(GSIPacket packet) {
		sendPacketToAll(packet, GSVersion.MINIMUM_VERSION);
	}
	
	public void sendPacketToAll(GSIPacket packet, GSVersion minExtensionVersion);

	default public void sendPacketToAllExcept(GSIPacket packet, ServerPlayerEntity player) {
		sendPacketToAllExcept(packet, GSVersion.MINIMUM_VERSION, player);
	}
	
	public void sendPacketToAllExcept(GSIPacket packet, GSVersion minExtensionVersion, ServerPlayerEntity player);

	public ServerPlayerEntity getPlayer(UUID playerUUID);
	
	public Collection<ServerPlayerEntity> getAllPlayers();
	
}
