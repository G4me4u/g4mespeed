package com.g4mesoft.packet;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.registry.GSSupplierRegistry;

public class GSPacketRegistryList {

	private final Map<GSExtensionUID, GSSupplierRegistry<Integer, GSIPacket>> uidToRegistry;
	
	private final Map<Class<? extends GSIPacket>, GSExtensionUID> uidCache;
	private final Map<Class<? extends GSIPacket>, Long> idCache;

	public GSPacketRegistryList() {
		uidToRegistry = new HashMap<>();
		uidCache = new IdentityHashMap<>();
		idCache = new IdentityHashMap<>();
	}

	public GSExtensionUID getPacketExtensionUID(Class<? extends GSIPacket> packetClazz) {
		GSExtensionUID extensionUid = uidCache.get(packetClazz);
		if (extensionUid != null)
			return extensionUid;
	
		for (Map.Entry<GSExtensionUID, GSSupplierRegistry<Integer, GSIPacket>> entry : uidToRegistry.entrySet()) {
			GSSupplierRegistry<Integer, GSIPacket> registry = entry.getValue();

			if (registry.containsElement(packetClazz)) {
				extensionUid = entry.getKey();
				uidCache.put(packetClazz, extensionUid);
				return extensionUid;
			}
		}
		
		return null;
	}
	
	public long getPacketId(Class<? extends GSIPacket> packetClazz) {
		Long cache = idCache.get(packetClazz);
		if (cache != null)
			return cache.longValue();
	
		GSExtensionUID extensionUid = getPacketExtensionUID(packetClazz);

		if (extensionUid != null) {
			GSSupplierRegistry<Integer, GSIPacket> registry = uidToRegistry.get(extensionUid);
			
			int uid = extensionUid.getValue();
			int id = registry.getIdentifier(packetClazz);

			long value = ((long)uid << 32L) | (long)id;
			idCache.put(packetClazz, Long.valueOf(value));

			return value;
		}
		
		return -1L;
	}
	
	public GSIPacket createNewPacket(long packetId) {
		GSExtensionUID extensionUid = new GSExtensionUID((int)(packetId >> 32L));
		GSSupplierRegistry<Integer, GSIPacket> registry = uidToRegistry.get(extensionUid);
		
		return (registry != null) ? registry.createNewElement((int)packetId) : null;
	}

	public void addPacketRegistry(GSExtensionUID uid, GSSupplierRegistry<Integer, GSIPacket> registry) {
		if (uidToRegistry.containsKey(uid))
			throw new IllegalStateException("The UID is already registered!");
		if (registry == null)
			throw new NullPointerException("registry is null!");
		
		uidToRegistry.put(uid, registry);
	}
}
