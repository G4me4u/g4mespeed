package com.g4mesoft.core;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import com.g4mesoft.core.client.GSIClientModuleManager;
import com.g4mesoft.core.server.GSIServerModuleManager;
import com.g4mesoft.setting.GSSettingManager;

public interface GSIModuleManager {

	/**
	 * Adds the given module to the module manager.
	 * 
	 * @param module - the module to be added
	 * 
	 * @throws IllegalStateException if a module with the same class was already
	 *                               added.
	 */
	public void addModule(GSIModule module);

	/**
	 * Retrieves the module with the given {@code moduleClazz} from those modules
	 * that have been added through the {@link #addModule(GSIModule)} method.
	 * 
	 * @param <M> - the module type
	 * @param moduleClazz - the module class
	 * 
	 * @return The module with the given class
	 */
	public <M extends GSIModule> M getModule(Class<M> moduleClazz);
	
	/**
	 * @return An unmodifiable list of all the modules that have been added through
	 *         the {@link #addModule(GSIModule)} method.
	 */
	public List<GSIModule> getModules();

	/**
	 * Invokes the given consumer only if this is a {@link GSIClientModuleManager}.
	 * 
	 * @param consumer - the consumer to be run on the client
	 */
	public void runOnClient(Consumer<GSIClientModuleManager> consumer);

	/**
	 * Invokes the given consumer only if this is a {@link GSIServerModuleManager}.
	 * 
	 * @param consumer - the consumer to be run on the server
	 */
	public void runOnServer(Consumer<GSIServerModuleManager> consumer);
	
	/**
	 * @return True, if this is a {@link GSIClientModuleManager}, false otherwise.
	 */
	public boolean isClient();
	
	/**
	 * @return True, if this is a {@link GSIServerModuleManager}, false otherwise.
	 */
	default public boolean isServer() {
		return !isClient();
	}
	
	/**
	 * @return The local setting manager of this module manager.
	 */
	public GSSettingManager getSettingManager();

	/**
	 * @return The local directory where cached files should be stored. Note that
	 *         this directory does not depend on the currently open world.
	 */
	public File getCacheFile();
	
}
