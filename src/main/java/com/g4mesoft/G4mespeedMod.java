package com.g4mesoft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.packet.GSPacketManager;

import net.fabricmc.api.ModInitializer;

public class G4mespeedMod implements ModInitializer {

	public static final String MOD_NAME = "G4mespeed";
	public static final GSVersion GS_VERSION = new GSVersion(1, 0, 6);
	public static final byte CORE_EXTENSION_UID = (byte)0x00;

	public static final Logger GS_LOGGER = LogManager.getLogger(MOD_NAME);

	private static G4mespeedMod instance;
	
	private GSPacketManager packetManager;
	private GSCarpetCompat carpetCompat;
	
	private GSCoreExtension coreExtension;
	
	private static final List<GSIExtension> extensions;
	private static final Map<Byte, GSIExtension> idToExtension;
	private static final List<GSIExtensionListener> extensionListeners;
	
	static {
		extensions = new ArrayList<GSIExtension>();
		idToExtension = new HashMap<Byte, GSIExtension>();
		extensionListeners = new ArrayList<GSIExtensionListener>();
	}
	
	public G4mespeedMod() {
	}
	
	@Override
	@GSCoreOverride
	public void onInitialize() {
		instance = this;

		packetManager = new GSPacketManager();
		
		carpetCompat = new GSCarpetCompat();
		carpetCompat.detectCarpet();
		
		coreExtension = new GSCoreExtension();

		addExtension(coreExtension);
		
		GS_LOGGER.info("G4mespeed " + GS_VERSION.getVersionString() + " initialized!");
	}
	
	public static void addExtension(GSIExtension extension) {
		byte uid = extension.getUniqueId();
		if (idToExtension.containsKey(uid))
			throw new IllegalArgumentException("Duplicate extension ID: " + uid);
		
		idToExtension.put(uid, extension);
		extensions.add(extension);
		
		if (instance != null)
			dispatchExtensionAddedEvent(extension);
	}
	
	public static List<GSIExtension> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}
	
	public static void addExtensionListener(GSIExtensionListener listener) {
		extensionListeners.add(listener);
	}

	public static void removeExtensionListener(GSIExtensionListener listener) {
		extensionListeners.remove(listener);
	}

	private static void dispatchExtensionAddedEvent(GSIExtension extension) {
		for (GSIExtensionListener listener : extensionListeners)
			listener.extensionAdded(extension);
	}
	
	public GSPacketManager getPacketManager() {
		return packetManager;
	}

	public GSCarpetCompat getCarpetCompat() {
		return carpetCompat;
	}

	public GSCoreExtension getCoreExtension() {
		return coreExtension;
	}
	
	public static G4mespeedMod getInstance() {
		return instance;
	}
}
