package com.g4mesoft.core.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtils;

public final class GSCarpetCompat {

	private static final String TICKSPEED_CLASSPATH = "carpet.helpers.TickSpeed";
	private static final String ADD_TICKRATE_LISTENER_METHOD_NAME = "addTickrateListener";
	private static final String TICKRATE_METHOD_NAME = "tickrate";
	private static final String MSPT_FIELD_NAME = "mspt";
	
	private boolean carpetDetected;
	private Method carpetAddTickrateListener;
	
	private BiConsumer<String, Float> carpetTickrateListener;
	
	private Field msptField;
	private float lastBroadcastCarpetTickrate;
	private boolean outdatedCompatMode;
	
	private final List<GSICarpetCompatTickrateListener> tickrateListeners;
	
	public GSCarpetCompat() {
		tickrateListeners = new ArrayList<GSICarpetCompatTickrateListener>();
	}
	
	private void reset() {
		carpetDetected = false;
		carpetAddTickrateListener = null;
		carpetTickrateListener = null;
		
		msptField = null;
		lastBroadcastCarpetTickrate = GSTpsModule.DEFAULT_TPS;
		outdatedCompatMode = false;
	}
	
	public void detectCarpet() {
		// Reset compatibility fields.
		reset();
		
		Class<?> tickspeedClazz = null;
		try {
			tickspeedClazz = Class.forName(TICKSPEED_CLASSPATH);
		} catch (Exception e) {
			// Carpet is not installed...
		}
		
		if (tickspeedClazz != null) {
			carpetDetected = true;
			
			G4mespeedMod.GS_LOGGER.info("Carpet mod detected!");
			
			try {
				carpetAddTickrateListener = tickspeedClazz.getDeclaredMethod(ADD_TICKRATE_LISTENER_METHOD_NAME, String.class, BiConsumer.class);
			} catch (Exception e) {
				// Carpet version is not up to date..!
			}

			if (carpetAddTickrateListener != null)
				carpetTickrateListener = establishTickrateLink(this::carpetTickrateChanged);
		
			if (carpetTickrateListener == null) {
				G4mespeedMod.GS_LOGGER.info("Carpet might not be up to date! Attempting to link using outdated compatibility.");
				
				Method tickrateMethod = null;
				try {
					tickrateMethod = tickspeedClazz.getDeclaredMethod(TICKRATE_METHOD_NAME, Float.TYPE);
					msptField = tickspeedClazz.getField(MSPT_FIELD_NAME);
				} catch (Exception e) {
					// This should really never happen, but if it does we should
					// make sure to print the stack trace for debugging.
					e.printStackTrace();
					
					G4mespeedMod.GS_LOGGER.info("Unable to establish link to carpet mod.");
				}
				
				if (tickrateMethod != null && msptField != null) {
					outdatedCompatMode = true;
					carpetTickrateListener = new GSOutdatedCarpetTickrateListener(tickrateMethod);
				}
			}
		}
	}

	public void addCarpetTickrateListener(GSICarpetCompatTickrateListener tickrateListener) {
		synchronized(tickrateListeners) {
			tickrateListeners.add(tickrateListener);
		}
	}

	public void removeCarpetTickrateListener(GSICarpetCompatTickrateListener tickrateListener) {
		synchronized(tickrateListeners) {
			tickrateListeners.remove(tickrateListener);
		}
	}
	
	private void carpetTickrateChanged(String modId, float tickrate) {
		if (!G4mespeedMod.MOD_NAME.equals(modId)) {
			synchronized(tickrateListeners) {
				lastBroadcastCarpetTickrate = tickrate;
				
				for (GSICarpetCompatTickrateListener tickrateListener : tickrateListeners)
					tickrateListener.onCarpetTickrateChanged(tickrate);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private BiConsumer<String, Float> establishTickrateLink(BiConsumer<String, Float> tickrateListener) {
		if (carpetDetected && carpetAddTickrateListener != null) {
			try {
				return (BiConsumer<String, Float>)carpetAddTickrateListener.invoke(null, G4mespeedMod.MOD_NAME, tickrateListener);
			} catch (Exception e) {
				// Handle silently
			}
		}
		return null;
	}

	public float getCarpetTickrate() {
		if (isOutdatedCompatMode() && msptField != null) {
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
		if (carpetTickrateListener != null && !GSMathUtils.equalsApproximate(getCarpetTickrate(), tickrate)) {
			carpetTickrateListener.accept(G4mespeedMod.MOD_NAME, Float.valueOf(tickrate));
			
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
	
	private static class GSOutdatedCarpetTickrateListener implements BiConsumer<String, Float> {

		private final Method tickrateMethod;
		
		public GSOutdatedCarpetTickrateListener(Method tickrateMethod) {
			this.tickrateMethod = tickrateMethod;
		}

		@Override
		public void accept(String modId, Float tickrate) {
			try {
				tickrateMethod.invoke(null, tickrate);
			} catch (Exception e) {
			}
		}
	}
}
