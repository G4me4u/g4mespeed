package com.g4mesoft;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.registry.GSSupplierRegistry;

public interface GSIExtension {

	public void init();
	
	public void registerPackets(GSSupplierRegistry<Integer, GSIPacket> registry);
	
	public void addClientModules(GSClientController controller);
	
	public void addServerModules(GSServerController controller);

	public String getTranslationPath();
	
	public GSExtensionInfo getInfo();
	
}
