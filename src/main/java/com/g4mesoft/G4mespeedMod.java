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
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.packet.GSPacketManager;

import net.fabricmc.api.ModInitializer;

public class G4mespeedMod implements ModInitializer {

	public static final GSExtensionUID INVALID_EXTENSION_UID = new GSExtensionUID(0xFFFFFFFF);

	public static final Logger GS_LOGGER = LogManager.getLogger("G4mespeed Core");

	private static G4mespeedMod instance = null;
	private static boolean initialized = false;
	
	private GSPacketManager packetManager;
	private GSCarpetCompat carpetCompat;
	
	private GSCoreExtension coreExtension;
	
	private static final List<GSIExtension> extensions = new ArrayList<GSIExtension>();
	private static final Map<GSExtensionUID, GSIExtension> idToExtension = new HashMap<GSExtensionUID, GSIExtension>();
	private static final List<GSIExtensionListener> extensionListeners = new ArrayList<GSIExtensionListener>();
	
	private static GSExtensionInfo[] extensionInfoCache = new GSExtensionInfo[0];
	
	public G4mespeedMod() {
	}
	
	@Override
	@GSCoreOverride
	public void onInitialize() {
		instance = this;
		
		coreExtension = new GSCoreExtension();
		addExtension(coreExtension);

		packetManager = new GSPacketManager();
		
		carpetCompat = new GSCarpetCompat();
		carpetCompat.detectCarpet();
		
		GS_LOGGER.info("G4mespeed " + GSCoreExtension.VERSION + " initialized!");
		
		for (GSIExtension extension : extensions)
			extension.init();
	
		initialized = true;
	}
	
	public static void addExtension(GSIExtension extension) {
		synchronized (extensions) {
			GSExtensionUID uid = extension.getInfo().getUniqueId();
			
			if (INVALID_EXTENSION_UID.equals(uid))
				throw new IllegalArgumentException("Invalid extension ID: " + uid);
			
			if (idToExtension.containsKey(uid))
				throw new IllegalArgumentException("Duplicate extension ID: " + uid);
			
			idToExtension.put(uid, extension);
			extensions.add(extension);
		}

		if (initialized) {
			extension.init();
			
			dispatchExtensionAddedEvent(extension);
		}
	}
	
	public static List<GSIExtension> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}
	
	public static void addExtensionListener(GSIExtensionListener listener) {
		synchronized (extensionListeners) {
			extensionListeners.add(listener);
		}
	}

	public static void removeExtensionListener(GSIExtensionListener listener) {
		synchronized (extensionListeners) {
			extensionListeners.remove(listener);
		}
	}

	public static GSExtensionInfo getExtensionInfo(GSExtensionUID extensionUid) {
		return idToExtension.get(extensionUid).getInfo();
	}
	
	public static GSExtensionInfo[] getAllExtensionInfo() {
		synchronized (extensions) {
			int numExtensions = extensions.size();
			
			if (numExtensions != extensionInfoCache.length) {
				extensionInfoCache = new GSExtensionInfo[numExtensions];
			
				for (int i = 0; i < numExtensions; i++)
					extensionInfoCache[i] = extensions.get(i).getInfo();
			}
		}
		
		return extensionInfoCache;
	}
	
	private static void dispatchExtensionAddedEvent(GSIExtension extension) {
		synchronized (extensionListeners) {
			for (GSIExtensionListener listener : extensionListeners)
				listener.extensionAdded(extension);
		}
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
