package com.g4mesoft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.core.compat.GSTweakerooCompat;
import com.g4mesoft.packet.GSPacketManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class G4mespeedMod implements ModInitializer {

	public static final GSExtensionUID INVALID_EXTENSION_UID = new GSExtensionUID(0xFFFFFFFF);

	public static final Logger GS_LOGGER = LogManager.getLogger("G4mespeed Core");

	private static G4mespeedMod instance = null;
	private static boolean initialized = false;
	
	private GSPacketManager packetManager;
	private GSCarpetCompat carpetCompat;
	private GSTweakerooCompat tweakerooCompat;
	
	private static final List<GSIExtension> extensions = new ArrayList<>();
	private static final Set<GSExtensionUID> extensionIds = new HashSet<>();
	private static final List<GSIExtensionListener> extensionListeners = new ArrayList<>();
	
	private static final GSExtensionInfoList extensionInfoList = new GSExtensionInfoList();
	
	public G4mespeedMod() {
	}
	
	@Override
	@GSCoreOverride
	public void onInitialize() {
		instance = this;

		packetManager = new GSPacketManager();
		
		carpetCompat = new GSCarpetCompat();
		carpetCompat.detect();
		tweakerooCompat = new GSTweakerooCompat();
		tweakerooCompat.detect();
		
		// Detect g4mespeed extensions from installed mods
		FabricLoader.getInstance().getEntrypointContainers("g4mespeed", GSIExtension.class).forEach(container -> {
			ModMetadata metadata = container.getProvider().getMetadata();
			String modid = metadata.getId();
			try {
				GSIExtension extension = container.getEntrypoint();
				String name = extension.getInfo().getName();
				addExtension(extension);
				GS_LOGGER.info("Added extension '{}' provided by '{}'", name, modid);
			} catch (Throwable e) {
				GS_LOGGER.error("Mod '{}' provides a broken implementation of GSIExtension", modid, e);
			}
		});
		
		// Initialize extensions & extension packets.
		for (GSIExtension extension : extensions)
			extension.init();
		packetManager.init();
		
		initialized = true;

		GS_LOGGER.info("G4mespeed {} initialized!", GSCoreExtension.VERSION);
	}
	
	private static void addExtension(GSIExtension extension) {
		if (extension == null)
			throw new IllegalArgumentException("extension is null");
		
		synchronized (extensions) {
			GSExtensionUID uid = extension.getInfo().getUniqueId();
			
			if (INVALID_EXTENSION_UID.equals(uid))
				throw new IllegalArgumentException("Invalid extension ID: " + uid);
			if (!extensionIds.add(uid))
				throw new IllegalArgumentException("Duplicate extension ID: " + uid);
			
			extensions.add(extension);
			extensionInfoList.addInfo(extension.getInfo());
		}

		if (initialized) {
			extension.init();
			
			dispatchExtensionAddedEvent(extension);
		}
	}
	
	public static void addExtensionListener(GSIExtensionListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		synchronized (extensionListeners) {
			extensionListeners.add(listener);
		}
	}

	public static void removeExtensionListener(GSIExtensionListener listener) {
		synchronized (extensionListeners) {
			extensionListeners.remove(listener);
		}
	}

	private static void dispatchExtensionAddedEvent(GSIExtension extension) {
		synchronized (extensionListeners) {
			for (GSIExtensionListener listener : extensionListeners)
				listener.extensionAdded(extension);
		}
	}
	
	/*
	 * Not thread safe! It is recommended only to invoke these method after the
	 * invocation of the main Minecraft method. This should ensure that no further
	 * extensions are added to the collection, since extensions must be added
	 * during the fabric initialization.
	 */
	
	public static List<GSIExtension> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}
	
	public static GSExtensionInfoList getExtensionInfoList() {
		return extensionInfoList;
	}
	
	/**
	 * Must be invoked after {@link #onInitialize()} has been called.
	 * @return the packet manager
	 */
	public static GSPacketManager getPacketManager() {
		return getInstance().packetManager;
	}

	/**
	 * Must be invoked after {@link #onInitialize()} has been called.
	 * @return the Carpet compatibility handler
	 */
	public static GSCarpetCompat getCarpetCompat() {
		return getInstance().carpetCompat;
	}

	/**
	 * Must be invoked after {@link #onInitialize()} has been called.
	 * @return the Tweakeroo compatibility handler
	 */
	public static GSTweakerooCompat getTweakerooCompat() {
		return getInstance().tweakerooCompat;
	}

	private static G4mespeedMod getInstance() {
		return instance;
	}
}
