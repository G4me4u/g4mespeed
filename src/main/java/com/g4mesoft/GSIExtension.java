package com.g4mesoft;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSPacketRegistry;

public interface GSIExtension {

	public void registerPackets(GSPacketRegistry registry);
	
	public void addClientModules(GSControllerClient controller);
	
	public void addServerModules(GSControllerServer controller);
	
	public String getName();
	
	public byte getUniqueId();
	
}
