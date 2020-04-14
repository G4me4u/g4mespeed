package com.g4mesoft;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.registry.GSElementRegistry;

public interface GSIExtension {

	public void registerPackets(GSElementRegistry<GSIPacket> registry);
	
	public void addClientModules(GSControllerClient controller);
	
	public void addServerModules(GSControllerServer controller);

	public String getTranslationPath();
	
	public String getName();
	
	public byte getUniqueId();
	
}
