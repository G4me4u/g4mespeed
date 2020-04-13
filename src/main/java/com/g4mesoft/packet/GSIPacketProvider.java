package com.g4mesoft.packet;

public interface GSIPacketProvider<T extends GSIPacket> {

	public T createNewPacket();
	
}
