package com.g4mesoft.core.server;

import java.util.Collection;
import java.util.UUID;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.setting.GSSettingManager;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIServerModuleManager extends GSIModuleManager {

	/**
	 * Checks whether the player is known to have the given extension installed.
	 * The version of the extension does not affect the output of this method.
	 * <br><br>
	 * <i>Note: This method only returns accurate results after the player has
	 *          sent an extension info packet.</i>
	 * 
	 * @param player - the player to check
	 * @param extensionUid - the UID of the extension.
	 * 
	 * @return True if the player has some version of the given extension installed.
	 * 
	 * @see #isExtensionInstalled(ServerPlayerEntity, GSExtensionUID, GSVersion)
	 */
	public boolean isExtensionInstalled(ServerPlayerEntity player, GSExtensionUID extensionUid);

	/**
	 * Checks whether the player is known to have the given extension installed.
	 * <br><br>
	 * <i>Note: This method only returns accurate results after the player has
	 *          sent an extension info packet.</i>
	 * 
	 * @param player - the player to check
	 * @param extensionUid - the UID of the extension.
	 * @param minimumVersion - the required minimum version of the corresponding
	 *                         extension installed on the player.
	 * 
	 * @return True if the player has the given extension installed at the given
	 *         version or above. False otherwise.
	 * 
	 * @see #isExtensionInstalled(ServerPlayerEntity, GSExtensionUID, GSVersion)
	 */
	public boolean isExtensionInstalled(ServerPlayerEntity player, GSExtensionUID extensionUid, GSVersion minimumVersion);
	
	/**
	 * Retrieves the extension info of the given player and extension UID. If the
	 * extension is not installed, this method returns an extension info where the
	 * version is invalid.
	 * <br><br>
	 * <i>Note: This method only returns accurate results after the player has
	 *          sent an extension info packet.</i>
	 * 
	 * @param extensionUid - the UID of the extension.
	 * 
	 * @return The extension info received from the player.
	 */
	public GSExtensionInfo getExtensionInfo(ServerPlayerEntity player, GSExtensionUID extensionUid);
	
	/**
	 * Send the given packet to the player. The packet will only be sent if the
	 * player has the extension installed that registered the packet. I.e. if
	 * {@link #isExtensionInstalled(ServerPlayerEntity, GSExtensionUID)} returns
	 * true for the given player and corresponding packet extension UID.
	 * 
	 * @param player - the player who should receive the packet
	 * @param packet - the packet to be sent to the player.
	 * 
	 * @see #sendPacket(GSIPacket, ServerPlayerEntity, GSVersion)
	 */
	default public void sendPacket(GSIPacket packet, ServerPlayerEntity player) {
		sendPacket(packet, player, GSVersion.MINIMUM_VERSION);
	}

	/**
	 * Send the given packet to the player. The packet will only be sent if the
	 * player has the extension installed that registered the packet, <i>and</i>
	 * the extension version is at least {@code minExtensionVersion}. I.e. if
	 * {@link #isExtensionInstalled(ServerPlayerEntity, GSExtensionUID, GSVersion)}
	 * returns true for the given player, corresponding packet extension UID, and
	 * version.
	 * 
	 * @param player - the player who should receive the packet
	 * @param packet - the packet to be sent to the player.
	 * @param minExtensionVersion - the required minimum version of the corresponding
	 *                              extension installed on the player.
	 */
	public void sendPacket(GSIPacket packet, ServerPlayerEntity player, GSVersion minExtensionVersion);

	/**
	 * Sends the given packet to all players. The packet will only be sent to a
	 * player if they have the extension installed that registered the packet. I.e.
	 * if {@link #isExtensionInstalled(ServerPlayerEntity, GSExtensionUID)} returns
	 * true for the given player and corresponding packet extension UID.
	 * 
	 * @param packet - the packet to be sent to the players.
	 */
	default public void sendPacketToAll(GSIPacket packet) {
		sendPacketToAll(packet, GSVersion.MINIMUM_VERSION);
	}
	
	/**
	 * Sends the given packet to all players. The packet will only be sent to a
	 * player if they have the extension installed that registered the packet,
	 * <i>and</i> the extension version is at least {@code minExtensionVersion}. I.e.
	 * if {@link #isExtensionInstalled(ServerPlayerEntity, GSExtensionUID, GSVersion)}
	 * returns true for the given player, corresponding packet extension UID, and
	 * version.
	 * 
	 * @param packet - the packet to be sent to the players.
	 * @param minExtensionVersion - the required minimum version of the corresponding
	 *                              extension installed on the player.
	 */
	public void sendPacketToAll(GSIPacket packet, GSVersion minExtensionVersion);

	/**
	 * Like {@link #sendPacketToAll(GSIPacket)}, sends the given packet to all players,
	 * except for the player given as argument. If {@code player == null} is given then
	 * this method is equivalent to {@link #sendPacketToAll(GSIPacket)}.
	 * 
	 * @param packet - the packet to send to all players except {@code player}.
	 * @param player - the player which should not receive the packet
	 */
	default public void sendPacketToAllExcept(GSIPacket packet, ServerPlayerEntity player) {
		sendPacketToAllExcept(packet, GSVersion.MINIMUM_VERSION, player);
	}
	
	/**
	 * Like {@link #sendPacketToAll(GSIPacket, GSVersion)}, sends the given packet to all
	 * players, except for the player given as argument. If {@code player == null} is given
	 * then this method is equivalent to {@link #sendPacketToAll(GSIPacket, GSVersion)}.
	 * 
	 * @param packet - the packet to send to all players except {@code player}.
	 * @param minExtensionVersion - the required minimum version of the corresponding
	 *                              extension installed on the player.
	 * @param player - the player which should not receive the packet
	 */
	public void sendPacketToAllExcept(GSIPacket packet, GSVersion minExtensionVersion, ServerPlayerEntity player);

	/**
	 * Retrieves the player with the given UUID.
	 * 
	 * @param playerUUID - the UUID of the player to retrieve
	 * 
	 * @return The player with the given UUID or {@code null} if no currently online
	 *         player matches the UUID.
	 */
	public ServerPlayerEntity getPlayer(UUID playerUUID);
	
	/**
	 * @return An unmodifiable collection of all currently online players.
	 */
	public Collection<ServerPlayerEntity> getAllPlayers();
	
	/**
	 * @return The instance of the {@link MinecraftServer} which is currently running.
	 */
	public MinecraftServer getServer();
	
	/**
	 * @return The global (across worlds) setting manager of this module manager.
	 */
	default public GSSettingManager getGlobalSettingManager() {
		return getSettingManager();
	}
	
	/**
	 * @return The global (across worlds) setting manager of this module manager.
	 */
	@Override
	public GSSettingManager getSettingManager();

	/**
	 * @return The world specific setting manager of this module manager.
	 */
	public GSSettingManager getWorldSettingManager();
	
}
