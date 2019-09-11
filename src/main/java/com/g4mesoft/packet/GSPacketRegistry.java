package com.g4mesoft.packet;

import java.util.HashMap;
import java.util.Map;

public class GSPacketRegistry {

	private final Class<? extends GSIPacket>[] packets;
	private final Map<Class<? extends GSIPacket>, Integer> packetToIndex;
	
	public GSPacketRegistry(Class<? extends GSIPacket>[] packets) {
		this.packets = packets;
		packetToIndex = new HashMap<Class<? extends GSIPacket>, Integer>();
		
		for (int i = 0; i < packets.length; i++)
			packetToIndex.put(packets[i], i);
	}
	
	public int getPacketIndex(GSIPacket packet) {
		return getPacketIndex(packet.getClass());
	}

	public int getPacketIndex(Class<? extends GSIPacket> packetClazz) {
		Integer index = packetToIndex.get(packetClazz);
		return (index == null) ? -1 : index.intValue();
	}
	
	public Class<? extends GSIPacket> getPacketClass(int index) {
		if (index < 0 || index >= packets.length)
			return null;
		return packets[index];
	}
}
