package com.g4mesoft.packet;

import java.util.HashMap;
import java.util.Map;

public class GSPacketRegistryList {

	private final Map<Byte, GSPacketRegistry> uidToRegistry;
	private final Map<Class<? extends GSIPacket>, Short> idCache;

	public GSPacketRegistryList() {
		uidToRegistry = new HashMap<Byte, GSPacketRegistry>();
		idCache = new HashMap<Class<? extends GSIPacket>, Short>();
	}

	public int getPacketId(Class<? extends GSIPacket> packetClazz) {
		Short cache = idCache.get(packetClazz);
		if (cache != null)
			return cache;
	
		for (Map.Entry<Byte, GSPacketRegistry> entry : uidToRegistry.entrySet()) {
			GSPacketRegistry registry = entry.getValue();

			if (registry.containsPacket(packetClazz)) {
				int uid = entry.getKey();
				int id = registry.getIdFromPacket(packetClazz);

				int value = ((uid << 8) | (id & 0xFF)) & 0xFFFF;
				idCache.put(packetClazz, Short.valueOf((short)value));
				return value;
			}
		}
		
		return -1;
	}
	
	public GSIPacket createNewPacket(short packetId) {
		GSPacketRegistry registry = uidToRegistry.get((byte)(packetId >>> 8));
		return (registry == null) ? null : registry.createNewPacket(packetId & 0xFF);
	}
	
	public void addPacketRegistry(byte uid, GSPacketRegistry registry) {
		if (uidToRegistry.containsKey(uid))
			throw new IllegalStateException("The UID is already registered!");
		if (registry == null)
			throw new NullPointerException("registry is null!");
		uidToRegistry.put(uid, registry);
	}
}
