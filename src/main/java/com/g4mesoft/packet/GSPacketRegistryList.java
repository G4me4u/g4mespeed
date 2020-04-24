package com.g4mesoft.packet;

import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.registry.GSElementRegistry;

public class GSPacketRegistryList {

	private final Map<Byte, GSElementRegistry<GSIPacket>> uidToRegistry;
	private final Map<Class<? extends GSIPacket>, Short> idCache;

	public GSPacketRegistryList() {
		uidToRegistry = new HashMap<Byte, GSElementRegistry<GSIPacket>>();
		idCache = new HashMap<Class<? extends GSIPacket>, Short>();
	}

	public int getPacketId(Class<? extends GSIPacket> packetClazz) {
		Short cache = idCache.get(packetClazz);
		if (cache != null)
			return cache;
	
		for (Map.Entry<Byte, GSElementRegistry<GSIPacket>> entry : uidToRegistry.entrySet()) {
			GSElementRegistry<GSIPacket> registry = entry.getValue();

			if (registry.containsElement(packetClazz)) {
				int uid = entry.getKey();
				int id = registry.getIdFromElement(packetClazz);

				int value = ((uid << 8) | (id & 0xFF)) & 0xFFFF;
				idCache.put(packetClazz, Short.valueOf((short)value));
				return value;
			}
		}
		
		return -1;
	}
	
	public GSIPacket createNewPacket(short packetId) {
		GSElementRegistry<GSIPacket> registry = uidToRegistry.get((byte)(packetId >>> 8));
		return (registry == null) ? null : registry.createNewElement(packetId & 0xFF);
	}
	
	public void addPacketRegistry(byte uid, GSElementRegistry<GSIPacket> registry) {
		if (uidToRegistry.containsKey(uid))
			throw new IllegalStateException("The UID is already registered!");
		if (registry == null)
			throw new NullPointerException("registry is null!");
		uidToRegistry.put(uid, registry);
	}
}
