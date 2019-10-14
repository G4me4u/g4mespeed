package com.g4mesoft.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.setting.GSSettingManager;

import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public abstract class GSController implements GSIModuleManager {

	private static final String SETTINGS_PATH = "settings.cfg";
	
	protected static final String CACHE_DIR_NAME = "g4mespeed/cache";
	protected static final String INTEGRATED_CACHE_DIR_NAME = "g4mespeed/integrated/cache";
	
	protected final GSSettingManager settings;
	
	protected final List<GSIModule> modules;
	
	protected final GSTpsModule tpsModule;
//	protected final GSProbeModule probeModule;
	protected final GSTranslationModule translationModule;
	
	public GSController() {
		settings = new GSSettingManager();
		
		modules = new ArrayList<GSIModule>();
		
		tpsModule = new GSTpsModule();
//		probeModule = new GSProbeModule();
		translationModule = new GSTranslationModule();
	}

	protected void onStart() {
		settings.loadSettings(getSettingsFile());

		initModules();
	}
	
	protected void onStop() {
		settings.saveSettings(getSettingsFile());
	}
	
	protected void initModules() {
		addModule(tpsModule);
//		addModule(probeModule);
		addModule(translationModule);
	}
	
	
	public void addModule(GSIModule module) {
		modules.add(module);
		module.init(this);
	}
	
	public void tick() {
		for (GSIModule module : modules)
			module.tick();
	}
	
	public GSTpsModule getTpsModule() {
		return tpsModule;
	}
	
	public GSTranslationModule getTranslationModule() {
		return translationModule;
	}
	
	@Override
	public List<GSIModule> getModules() {
		return modules;
	}
	
	@Override
	public GSSettingManager getSettingManager() {
		return settings;
	}

	private File getSettingsFile() {
		return new File(getCacheFile(), SETTINGS_PATH);
	}
	
	public static GSController getInstanceOnThread() {
		if (GSControllerClient.getInstance().isOwnedThread())
			return GSControllerClient.getInstance();
		
		if (GSControllerServer.getInstance().isOwnedThread())
			return GSControllerServer.getInstance();
		
		return null;
	}
	
	public abstract boolean isOwnedThread();
		
	public abstract Packet<?> encodeCustomPayload(Identifier identifier, PacketByteBuf buffer);

	public abstract boolean isClient();

	public abstract GSVersion getVersion();
	
}
