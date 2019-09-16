package com.g4mesoft.core;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public abstract class GSController implements GSIModuleManager {

	protected final List<GSIModule> modules;
	protected final GSTpsModule tpsModule;
	
	public GSController() {
		modules = new ArrayList<GSIModule>();
		tpsModule = new GSTpsModule();
		
		modules.add(tpsModule);
		
		initModules();
	}
	
	private void initModules() {
		for (GSIModule module : modules)
			module.init(this);
	}
	
	public GSTpsModule getTpsModule() {
		return tpsModule;
	}
	
	@Override
	public List<GSIModule> getModules() {
		return modules;
	}
	
	public abstract Packet<?> encodeCustomPayload(Identifier identifier, PacketByteBuf buffer);

	public abstract boolean isClient();

	public abstract int getVersion();
	
}
