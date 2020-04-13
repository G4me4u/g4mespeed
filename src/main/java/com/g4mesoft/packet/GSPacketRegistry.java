package com.g4mesoft.packet;

import java.util.HashMap;
import java.util.Map;

public class GSPacketRegistry {

	private final Map<Class<? extends GSIPacket>, Integer> packetToId;
	private final Map<Integer, Class<? extends GSIPacket>> idToPacket;
	private final Map<Integer, GSIPacketProvider<?>> idToPacketProvider;
	
	public GSPacketRegistry() {
		packetToId = new HashMap<Class<? extends GSIPacket>, Integer>();
		idToPacket = new HashMap<Integer, Class<? extends GSIPacket>>();
		idToPacketProvider = new HashMap<Integer, GSIPacketProvider<?>>();
	}
	
	public <T extends GSIPacket> void register(int id, Class<T> packetClazz, GSIPacketProvider<T> provider) {
		if (idToPacket.containsKey(id))
			throw new IllegalStateException("ID is already registered: " + id);
		if (packetToId.containsKey(packetClazz))
			throw new IllegalStateException("Packet class already registered: " + packetClazz);
		
		idToPacket.put(id, packetClazz);
		packetToId.put(packetClazz, id);
		idToPacketProvider.put(id, provider);
	}
	
	public Class<? extends GSIPacket> getPacketFromId(int id) {
		return idToPacket.get(id);
	}

	public boolean containsPacket(Class<? extends GSIPacket> packetClazz) {
		return packetToId.containsKey(packetClazz);
	}
	
	public int getIdFromPacket(Class<? extends GSIPacket> packetClazz) {
		Integer packetId = packetToId.get(packetClazz);
		return (packetId == null) ? 0 : packetId.intValue();
	}
	
	public GSIPacket createNewPacket(int id) {
		GSIPacketProvider<?> provider = idToPacketProvider.get(id);
		return (provider == null) ? null : provider.createNewPacket();
	}
}
