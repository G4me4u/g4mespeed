package com.g4mesoft.core.client;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.ui.renderer.GSIRenderable3D;
import com.g4mesoft.ui.renderer.GSIRenderer3D;

public interface GSIClientModuleManager extends GSIModuleManager {

	/**
	 * Checks whether the server is known to have the given extension installed.
	 * The version of the extension does not affect the output of this method.
	 * <br><br>
	 * <i>Note: This method only returns accurate results after the server has
	 *          sent an extension info packet.</i>
	 * 
	 * @param extensionUid - the UID of the extension.
	 * 
	 * @return True if the server has some version of the given extension installed.
	 * 
	 * @see #isServerExtensionInstalled(GSExtensionUID, GSVersion)
	 * 
	 * @see #isG4mespeedServer()
	 */
	public boolean isServerExtensionInstalled(GSExtensionUID extensionUid);

	/**
	 * Checks whether the server is known to have the given extension installed.
	 * <br><br>
	 * <i>Note: This method only returns accurate results after the server has
	 *          sent an extension info packet.</i>
	 * 
	 * @param extensionUid - the UID of the extension.
	 * @param minimumVersion - the required minimum version of the corresponding
	 *                         extension installed on the server.
	 * 
	 * @return True if the server has the given extension installed at the given
	 *         version or above. False otherwise.
	 * 
	 * @see #isG4mespeedServer()
	 */
	public boolean isServerExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion);

	/**
	 * Retrieves the extension info for the given extension UID on the server. If
	 * the extension is not installed, this method returns an extension info where
	 * the version is invalid.
	 * <br><br>
	 * <i>Note: This method only returns accurate results after the server has
	 *          sent an extension info packet.</i>
	 * 
	 * @param extensionUid - the UID of the extension.
	 * 
	 * @return The extension info received from the server.
	 * 
	 * @see #isG4mespeedServer()
	 */
	public GSExtensionInfo getServerExtensionInfo(GSExtensionUID extensionUid);
	
	/**
	 * Retrieves the list of extension info that was received from the server.
	 * <br><br>
	 * <i>Note: This method only returns accurate results after the server has
	 *          sent an extension info packet.</i>
	 * <br>
	 * <b>Note: DO NOT MODIFY the returned extension list!</b>
	 * 
	 * @return a list of extension info received from the server.
	 * 
	 * @see #isG4mespeedServer()
	 */
	public GSExtensionInfoList getServerExtensionInfoList();
	
	/**
	 * Whether the client is connected to a G4mespeed server. Note that the
	 * {@link GSIModule#onJoinG4mespeedServer(GSExtensionInfo)} will be
	 * invoked when this value changes.
	 * 
	 * @return True, if connected to a server and the client has received a
	 *         G4mespeed server extension info packet. I.e. server is verified
	 *         to have some version of G4mespeed installed. False otherwise.
	 */
	public boolean isG4mespeedServer();

	/**
	 * @return True, if the client is in a world, and does not have a screen
	 *         open. False otherwise.
	 */
	public boolean isInGame();

	/**
	 * Send the given packet to the server. The packet will only be sent if the
	 * server has the extension installed that registered the packet. I.e. if
	 * {@link #isServerExtensionInstalled(GSExtensionUID)} returns true for the
	 * corresponding packet extension UID.
	 * 
	 * @param packet - the packet to be sent to the server.
	 * 
	 * @see #sendPacket(GSIPacket, GSVersion)
	 */
	default public void sendPacket(GSIPacket packet) {
		sendPacket(packet, GSVersion.MINIMUM_VERSION);
	}

	/**
	 * Send the given packet to the server. The packet will only be sent if the
	 * server has the extension installed that registered the packet, <i>and</i>
	 * the extension version is at least {@code minExtensionVersion}. I.e. if
	 * {@link #isServerExtensionInstalled(GSExtensionUID, GSVersion)} returns
	 * true for the corresponding packet extension UID and version.
	 * 
	 * @param packet - the packet to be sent to the server.
	 * @param minExtensionVersion - the required minimum version of the corresponding
	 *                              extension installed on the server.
	 */
	public void sendPacket(GSIPacket packet, GSVersion minExtensionVersion);
	
	/**
	 * Add a renderable element that will be invoked in the phase corresponding
	 * to {@link GSIRenderable3D#getRenderPhase()}. The invocation will prepare
	 * the renderer corresponding to the given phase, and provide an instance of
	 * {@link GSIRenderer3D} that can be used to draw.
	 * 
	 * @param renderable - the renderable element to be added
	 */
	public void addRenderable(GSIRenderable3D renderable);

	/**
	 * Removes a renderable element that was previously added through the
	 * {@link #addRenderable(GSIRenderable3D)} method.
	 * 
	 * @param renderable - the renderable element to be removed
	 */
	public void removeRenderable(GSIRenderable3D renderable);
	
}
