package com.g4mesoft.core.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtil;

public final class GSCarpetCompat extends GSAbstractCompat {

	private static final String TICKSPEED_CLASSPATH = "carpet.helpers.TickSpeed";
	private static final String ADD_TICKRATE_LISTENER_METHOD_NAME = "addTickrateListener";
	private static final String TICKRATE_METHOD_NAME = "tickrate";
	private static final String MSPT_FIELD_NAME = "mspt";
	
	private static final String G4MESPEED_INTERFACE_NAME = "g4mespeed";
	
	private boolean carpetDetected;
	
	private BiConsumer<String, Float> carpetTickrateListener;
	
	private boolean outdatedCompatMode;
	private Field msptField;

	private float lastBroadcastCarpetTickrate;
	private final List<GSICarpetCompatTickrateListener> tickrateListeners;
	
	public GSCarpetCompat() {
		carpetDetected = false;
		
		carpetTickrateListener = null;
		
		outdatedCompatMode = false;
		msptField = null;
		
		lastBroadcastCarpetTickrate = GSTpsModule.DEFAULT_TPS;
		tickrateListeners = new ArrayList<>();
	}
	
	@Override
	public void detect() {
		Class<?> tickspeedClazz = findClassByName(TICKSPEED_CLASSPATH);
		if (tickspeedClazz == null) {
			// Tickspeed class either moved, or carpet is not installed.
			// Assume the latter and return silently.
			return;
		}
	
		carpetDetected = true;
		
		G4mespeedMod.GS_LOGGER.info("Carpet mod detected!");
		
		Method carpetAddTickrateListener = findDeclaredMethod(tickspeedClazz, ADD_TICKRATE_LISTENER_METHOD_NAME, String.class, BiConsumer.class);
		if (carpetAddTickrateListener != null) {
			BiConsumer<String, Float> listener = this::carpetTickrateChanged;
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
				outdatedCompatMode = true;
				carpetTickrateListener = new GSOutdatedCarpetTickrateListener(tickrateMethod);
			}
		}
	}

	public void addCarpetTickrateListener(GSICarpetCompatTickrateListener tickrateListener) {
		synchronized (tickrateListeners) {
			tickrateListeners.add(tickrateListener);
		}
	}

	public void removeCarpetTickrateListener(GSICarpetCompatTickrateListener tickrateListener) {
		synchronized (tickrateListeners) {
			tickrateListeners.remove(tickrateListener);
		}
	}
	
	private void carpetTickrateChanged(String modId, float tickrate) {
		if (!G4MESPEED_INTERFACE_NAME.equals(modId)) {
			synchronized (tickrateListeners) {
				lastBroadcastCarpetTickrate = tickrate;
				
				for (GSICarpetCompatTickrateListener tickrateListener : tickrateListeners)
					tickrateListener.onCarpetTickrateChanged(tickrate);
			}
		}
	}
	
	public float getCarpetTickrate() {
		if (outdatedCompatMode && msptField != null) {
			float tickrate = GSTpsModule.DEFAULT_TPS;
			
			try {
				tickrate = 1000.0f / msptField.getFloat(null);
			} catch (Exception e) {
				// Field inaccessible.. strange.
				msptField = null;
				outdatedCompatMode = false;
				carpetTickrateListener = null;
			}
			
			lastBroadcastCarpetTickrate = tickrate;
		}
		
		return lastBroadcastCarpetTickrate;
	}
	
	public void notifyTickrateChange(float tickrate) {
		if (carpetTickrateListener != null && !GSMathUtil.equalsApproximate(getCarpetTickrate(), tickrate)) {
			carpetTickrateListener.accept(G4MESPEED_INTERFACE_NAME, Float.valueOf(tickrate));
			
			// Assume the tickrate was set correctly.
			lastBroadcastCarpetTickrate = tickrate;
		}
	}
	
	public boolean isCarpetDetected() {
		return carpetDetected;
	}

	public boolean isTickrateLinked() {
		return (carpetTickrateListener != null);
	}
	
	public boolean isOutdatedCompatMode() {
		return outdatedCompatMode;
	}
	
	private class GSOutdatedCarpetTickrateListener implements BiConsumer<String, Float> {

		private final Method tickrateMethod;
		
		public GSOutdatedCarpetTickrateListener(Method tickrateMethod) {
			this.tickrateMethod = tickrateMethod;
		}

		@Override
		public void accept(String modId, Float tickrate) {
			invokeStatic(tickrateMethod, tickrate);
		}
	}
}
