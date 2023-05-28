package com.g4mesoft.core.compat;

import static com.g4mesoft.core.compat.GSCarpetCompat.G4MESPEED_INTERFACE_NAME;
import static com.g4mesoft.core.compat.GSCompatUtil.findClassByName;
import static com.g4mesoft.core.compat.GSCompatUtil.findDeclaredMethod;
import static com.g4mesoft.core.compat.GSCompatUtil.get;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSIModuleManager;

public class GSServerCarpetTickrateManager extends GSAbstractCarpetTickrateManager {

	private static final String SERVER_TRM_CLASSPATH = "carpet.helpers.ServerTickRateManager";
	private static final String ADD_TICKRATE_LISTENER_METHOD_NAME = "addTickrateListener";
	private static final String RUNS_NORMALLY_METHOD_NAME = "runsNormally";
	private static final String MINECRAFT_SERVER_INTERFACE_CLASSPATH = "carpet.fakes.MinecraftServerInterface";
	private static final String GET_TRM_METHOD_NAME = "getTickRateManager";

	private Method getTRMMethod;
	private Method runsNormallyMethod;
	private Method carpetAddTickrateListener;
	
	private Object tickrateManager;
	
	@Override
	public void onInit(GSIModuleManager manager) {
		if (getTRMMethod != null) {
			manager.runOnServer(managerServer -> {
				tickrateManager = get(managerServer.getServer(), getTRMMethod);
				if (tickrateManager != null && carpetAddTickrateListener != null) {
					BiConsumer<String, Float> listener = this::onCarpetTickrateChanged;
					carpetTickrateListener = get(tickrateManager,
							carpetAddTickrateListener, G4MESPEED_INTERFACE_NAME, listener);
					if (carpetTickrateListener == null) {
						// This should never happen but just in case it does,
						// never invoke this method again.
						carpetAddTickrateListener = null;
					}
				}
			});
		}
	}
	
	@Override
	public void onClose() {
		tickrateManager = null;
	}
	
	private boolean detect() {
		Class<?> sTRMClazz = findClassByName(SERVER_TRM_CLASSPATH);
		if (sTRMClazz == null) {
			// Likely, carpet is not up to date.
			return false;
		}
		
		G4mespeedMod.GS_LOGGER.info("Carpet mod detected server-side!");

		Class<?> mcServerClazz = findClassByName(MINECRAFT_SERVER_INTERFACE_CLASSPATH);
		if (mcServerClazz != null)
			getTRMMethod = findDeclaredMethod(mcServerClazz, GET_TRM_METHOD_NAME);
		if (getTRMMethod == null) {
			// Nothing else really matters here, but return true,
			// since we did detect the tickrate manager class.
			G4mespeedMod.GS_LOGGER.info("Unable to find server getTickRateManager method.");
		} else {
			carpetAddTickrateListener = findDeclaredMethod(sTRMClazz,
					ADD_TICKRATE_LISTENER_METHOD_NAME, String.class, BiConsumer.class);
			if (carpetAddTickrateListener == null)
				G4mespeedMod.GS_LOGGER.info("Unable to establish server link to carpet mod.");
			
			runsNormallyMethod = findDeclaredMethod(sTRMClazz, RUNS_NORMALLY_METHOD_NAME);
		}
		
		return true;
	}

	@Override
	public boolean runsNormally() {
		if (tickrateManager != null && runsNormallyMethod != null) {
			try {
				return get(tickrateManager, runsNormallyMethod);
			} catch (Exception e) {
				runsNormallyMethod = null;
			}
		}
		// Assume game runs normally
		return true;
	}
	
	@Override
	public boolean isPollingCompatMode() {
		return false;
	}

	public static GSICarpetTickrateManager create() {
		GSServerCarpetTickrateManager result = new GSServerCarpetTickrateManager();
		return result.detect() ? result : null;
	}
}
