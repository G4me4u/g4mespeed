package com.g4mesoft.core.compat;

import static com.g4mesoft.core.compat.GSCarpetCompat.G4MESPEED_INTERFACE_NAME;
import static com.g4mesoft.core.compat.GSCompatUtil.findClassByName;
import static com.g4mesoft.core.compat.GSCompatUtil.findDeclaredMethod;
import static com.g4mesoft.core.compat.GSCompatUtil.findField;
import static com.g4mesoft.core.compat.GSCompatUtil.getStatic;
import static com.g4mesoft.core.compat.GSCompatUtil.invokeStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.module.tps.GSTpsModule;

public class GSOutdatedCarpetTickrateManager extends GSAbstractCarpetTickrateManager {

	private static final String TICKSPEED_CLASSPATH = "carpet.helpers.TickSpeed";
	private static final String ADD_TICKRATE_LISTENER_METHOD_NAME = "addTickrateListener";
	private static final String TICKRATE_METHOD_NAME = "tickrate";
	private static final String MSPT_FIELD_NAME = "mspt";
	private static final String PROCESS_ENTITIES_FIELD_NAME = "process_entities";
	
	private boolean pollingCompatMode;
	private Field msptField;
	private Field processEntitiesField;
	
	@Override
	public void onInit(GSIModuleManager manager) {
		// Do nothing
	}
	
	@Override
	public void onClose() {
		// Do nothing
	}
	
	private boolean detect() {
		Class<?> tickspeedClazz = findClassByName(TICKSPEED_CLASSPATH);
		if (tickspeedClazz == null) {
			// Tickspeed class either moved, or carpet is not installed.
			// Assume the latter and return silently.
			return false;
		}
	
		G4mespeedMod.GS_LOGGER.info("Carpet mod detected!");
		
		Method carpetAddTickrateListener = findDeclaredMethod(tickspeedClazz, ADD_TICKRATE_LISTENER_METHOD_NAME, String.class, BiConsumer.class);
		if (carpetAddTickrateListener != null) {
			BiConsumer<String, Float> listener = this::onCarpetTickrateChanged;
			carpetTickrateListener = getStatic(carpetAddTickrateListener, G4MESPEED_INTERFACE_NAME, listener);
		}

		// In case invoking the above method failed for some reason...
		if (carpetTickrateListener == null) {
			G4mespeedMod.GS_LOGGER.info("Carpet might not be up to date! Attempting to link using outdated compatibility.");
				
			Method tickrateMethod = findDeclaredMethod(tickspeedClazz, TICKRATE_METHOD_NAME, Float.TYPE);
			msptField = findField(tickspeedClazz, MSPT_FIELD_NAME);
			
			if (tickrateMethod == null || msptField == null) {
				// This should never happen, but just to make sure print debug message.
				G4mespeedMod.GS_LOGGER.info("Unable to establish link to carpet mod.");
			} else {
				pollingCompatMode = true;
				carpetTickrateListener = new GSTickrateMethodListener(tickrateMethod);
			}
		}
		
		// Finally, get the process entities field to test if we are
		// freezing the game or not.
		processEntitiesField = findField(tickspeedClazz, PROCESS_ENTITIES_FIELD_NAME);
		
		return true;
	}
	
	@Override
	public boolean runsNormally() {
		if (processEntitiesField != null) {
			try {
				return processEntitiesField.getBoolean(null);
			} catch (Exception e) {
				processEntitiesField = null;
			}
		}
		// We do not know if the game is supposed to run normally,
		// simply assume that it does.
		return true;
	}

	@Override
	public float getTickrate() {
		if (pollingCompatMode && msptField != null) {
			float tickrate = GSTpsModule.DEFAULT_TPS;
			
			try {
				tickrate = 1000.0f / msptField.getFloat(null);
			} catch (Exception e) {
				// Field inaccessible.. strange.
				msptField = null;
				pollingCompatMode = false;
				carpetTickrateListener = null;
			}
			
			lastBroadcastCarpetTickrate = tickrate;
		}
		return super.getTickrate();
	}

	@Override
	public boolean isPollingCompatMode() {
		return pollingCompatMode;
	}
	
	public static GSICarpetTickrateManager create() {
		GSOutdatedCarpetTickrateManager result = new GSOutdatedCarpetTickrateManager();
		return result.detect() ? result : null;
	}
	
	private class GSTickrateMethodListener implements BiConsumer<String, Float> {

		private final Method tickrateMethod;
		
		public GSTickrateMethodListener(Method tickrateMethod) {
			this.tickrateMethod = tickrateMethod;
		}

		@Override
		public void accept(String modId, Float tickrate) {
			invokeStatic(tickrateMethod, tickrate);
		}
	}
}
