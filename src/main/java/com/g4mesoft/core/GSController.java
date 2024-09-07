package com.g4mesoft.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.GSIExtensionListener;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.setting.GSSettingManager;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public abstract class GSController implements GSIModuleManager, GSIExtensionListener {

	protected static final String SETTINGS_FILE_NAME = "settings.cfg";
	
	protected static final String CACHE_DIR_NAME = "g4mespeed/cache";
	protected static final String INTEGRATED_CACHE_DIR_NAME = "g4mespeed/integrated/cache";

	private static final Set<GSController> INSTANCES = new HashSet<>();
	
	protected final GSSettingManager settings;
	
	protected final List<GSIModule> modules;
	protected final Map<Class<? extends GSIModule>, GSIModule> clazzToModule;
	
	protected final GSTpsModule tpsModule;
	protected final GSTranslationModule translationModule;
	
	public GSController() {
		settings = new GSSettingManager();
		
		modules = new ArrayList<>();
		clazzToModule = new IdentityHashMap<>();
		
		tpsModule = new GSTpsModule();
		translationModule = new GSTranslationModule();
	}

	protected void onStart() {
		INSTANCES.add(this);
		
		settings.loadSettings(getSettingsFile());

		initModules();

		G4mespeedMod.addExtensionListener(this);
	}
	
	protected void onStop() {
		INSTANCES.remove(this);

		for (GSIModule module : modules)
			module.onClose();
		
		settings.saveSettings(getSettingsFile());
		settings.clearSettings();
		
		modules.clear();
		clazzToModule.clear();

		G4mespeedMod.removeExtensionListener(this);
	}
	
	protected void initModules() {
		addModule(tpsModule);
		addModule(translationModule);
		
		G4mespeedMod.getExtensions().forEach(this::addExtensionModules);
	}
	
	@Override
	public void addModule(GSIModule module) {
		Class<? extends GSIModule> clazz = module.getClass();
		if (clazzToModule.put(clazz, module) != null)
			throw new IllegalStateException("Module of class " + clazz.getName() + " already exists");
		
		modules.add(module);
		module.init(this);
	}
	
	public void tick(boolean paused) {
		for (GSIModule module : modules)
			module.tick(paused);
	}
	
	public GSTpsModule getTpsModule() {
		return tpsModule;
	}
	
	public GSTranslationModule getTranslationModule() {
		return translationModule;
	}
	
	@Override
	public <M extends GSIModule> M getModule(Class<M> moduleClazz) {
		@SuppressWarnings("unchecked")
		M module = (M)clazzToModule.get(moduleClazz);
		return module;
	}
	
	@Override
	public List<GSIModule> getModules() {
		return modules;
	}
	
	private File getSettingsFile() {
		return new File(getCacheFile(), SETTINGS_FILE_NAME);
	}
	
	@Override
	public GSSettingManager getSettingManager() {
		return settings;
	}
	
	public static GSController getInstanceOnThread() {
		for (GSController controller : INSTANCES) {
			if (controller.isThreadOwner())
				return controller;
		}
		return null;
	}
	
	@Override
	public void extensionAdded(GSIExtension extension) {
		addExtensionModules(extension);
	}
	
	protected abstract void addExtensionModules(GSIExtension extension);
	
	public abstract boolean isThreadOwner();
		
	public abstract Packet<?> createCustomPayload(Identifier identifier, PacketByteBuf buffer);

	public abstract boolean isClient();

}
