package com.g4mesoft.core.compat;

import static com.g4mesoft.core.compat.GSCompatUtil.findClassByName;
import static com.g4mesoft.core.compat.GSCompatUtil.findDeclaredMethod;
import static com.g4mesoft.core.compat.GSCompatUtil.get;
import static com.g4mesoft.core.compat.GSCompatUtil.invoke;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class GSClientCarpetTickrateManager extends GSAbstractCarpetTickrateManager {

	private static final String TRM_CLASSPATH = "carpet.helpers.TickRateManager";
	private static final String SET_TICKRATE_METHOD_NAME = "setTickRate";
	private static final String TICKRATE_METHOD_NAME = "tickrate";
	private static final String RUNS_NORMALLY_METHOD_NAME = "runsNormally";
	private static final String LEVEL_INTERFACE_CLASSPATH = "carpet.fakes.LevelInterface";
	private static final String GET_TRM_METHOD_NAME = "tickRateManager";

	private Method getTRMMethod;
	
	private Method tickrateMethod;
	private Method runsNormallyMethod;
	
	@Override
	public void onInit(GSIModuleManager manager) {
		// Do nothing
	}
	
	@Override
	public void onClose() {
		// Do nothing
	}
	
	private boolean detect() {
		Class<?> trmClazz = findClassByName(TRM_CLASSPATH);
		if (trmClazz == null) {
			// Likely, carpet is not up to date.
			return false;
		}

		// Already printed in GSServerCarpetTickrateManager
		G4mespeedMod.GS_LOGGER.info("Carpet mod detected client-side!");
		
		Class<?> levelInterfaceClazz = findClassByName(LEVEL_INTERFACE_CLASSPATH);
		if (levelInterfaceClazz != null)
			getTRMMethod = findDeclaredMethod(levelInterfaceClazz, GET_TRM_METHOD_NAME);
		if (getTRMMethod == null) {
			// Nothing else really matters here, but return true,
			// since we did detect the tickrate manager class.
			G4mespeedMod.GS_LOGGER.info("Unable to find level tickRateManager method.");
			return true;
		}

		// Note: we do not have listener support on the client
		//       for some reason. Use polling method instead.
		Method setTickrateMethod = findDeclaredMethod(trmClazz, SET_TICKRATE_METHOD_NAME, Float.TYPE);
		tickrateMethod = findDeclaredMethod(trmClazz, TICKRATE_METHOD_NAME);
		
		if (tickrateMethod == null || setTickrateMethod == null) {
			// This should never happen, but just to make sure print debug message.
			G4mespeedMod.GS_LOGGER.info("Unable to establish client link to carpet mod.");
		} else {
			carpetTickrateListener = new GSTickrateMethodListener(setTickrateMethod);
		}
		
		runsNormallyMethod = findDeclaredMethod(trmClazz, RUNS_NORMALLY_METHOD_NAME);
		
		return true;
	}
	
	public Object getTickrateManager() {
		if (getTRMMethod != null) {
			// Attempt to get through client world
			MinecraftClient client = MinecraftClient.getInstance();
			ClientWorld world = client.world;
			if (world != null)
				return get(world, getTRMMethod);
		}
		return null;
	}
	
	@Override
	public boolean runsNormally() {
		Object trmInstance = getTickrateManager();
		if (trmInstance != null && runsNormallyMethod != null) {
			try {
				return get(trmInstance, runsNormallyMethod);
			} catch (Exception e) {
				runsNormallyMethod = null;
			}
		}
		// Assume game runs normally
		return true;
	}

	@Override
	public float getTickrate() {
		Object trmInstance = getTickrateManager();
		if (trmInstance != null && tickrateMethod != null) {
			float tickrate = GSTpsModule.DEFAULT_TPS;
			
			try {
				tickrate = (Float)get(trmInstance, tickrateMethod);
			} catch (Exception e) {
				// Field inaccessible.. strange.
				tickrateMethod = null;
				carpetTickrateListener = null;
			}
			
			lastBroadcastCarpetTickrate = tickrate;
		}
		return super.getTickrate();
	}

	@Override
	public boolean isPollingCompatMode() {
		return true;
	}
	
	public static GSICarpetTickrateManager create() {
		GSClientCarpetTickrateManager result = new GSClientCarpetTickrateManager();
		return result.detect() ? result : null;
	}
	
	private class GSTickrateMethodListener implements BiConsumer<String, Float> {

		private final Method tickrateMethod;
		
		public GSTickrateMethodListener(Method tickrateMethod) {
			this.tickrateMethod = tickrateMethod;
		}

		@Override
		public void accept(String modId, Float tickrate) {
			Object trmInstance = getTickrateManager();
			if (trmInstance != null)
				invoke(trmInstance, tickrateMethod, tickrate);
		}
	}
}
