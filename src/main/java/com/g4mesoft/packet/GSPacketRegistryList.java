package com.g4mesoft.packet;

import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.registry.GSElementRegistry;

public class GSPacketRegistryList {

	private final Map<GSExtensionUID, GSElementRegistry<GSIPacket>> uidToRegistry;
	private final Map<Class<? extends GSIPacket>, Long> idCache;

	public GSPacketRegistryList() {
		uidToRegistry = new HashMap<GSExtensionUID, GSElementRegistry<GSIPacket>>();
		idCache = new HashMap<Class<? extends GSIPacket>, Long>();
	}

	public long getFullPacketId(Class<? extends GSIPacket> packetClazz) {
		Long cache = idCache.get(packetClazz);
		if (cache != null)
			return cache.longValue();
	
		for (Map.Entry<GSExtensionUID, GSElementRegistry<GSIPacket>> entry : uidToRegistry.entrySet()) {
			GSElementRegistry<GSIPacket> registry = entry.getValue();

			if (registry.containsElement(packetClazz)) {
				int uid = entry.getKey().getValue();
				int id = registry.getIdFromElement(packetClazz);

				long value = ((long)uid << 32L) | (long)id;
				idCache.put(packetClazz, Long.valueOf(value));
				return value;
			}
		}
		
		return -1;
	}
	
	public GSIPacket createNewPacket(long fullPacketId) {
		GSExtensionUID uid = getPacketExtensionUID(fullPacketId);
		int packetId = getBasicPacketId(fullPacketId);
		
		GSElementRegistry<GSIPacket> registry = getPacketRegistry(uid);
		return (registry == null) ? null : registry.createNewElement(packetId);
	}
	
	public GSElementRegistry<GSIPacket> getPacketRegistry(GSExtensionUID uid) {
		return uidToRegistry.get(uid);
	}
	
	public void addPacketRegistry(GSExtensionUID uid, GSElementRegistry<GSIPacket> registry) {
		if (uidToRegistry.containsKey(uid))
			throw new IllegalStateException("The UID is already registered!");
		if (registry == null)
			throw new NullPointerException("registry is null!");
		uidToRegistry.put(uid, registry);
	}
	
	public GSExtensionUID getPacketExtensionUID(long fullPacketId) {
		return new GSExtensionUID((int)(fullPacketId >>> 32L));
	}
	
	public int getBasicPacketId(long fullPacketId) {
		return (int)fullPacketId;
	}
}
