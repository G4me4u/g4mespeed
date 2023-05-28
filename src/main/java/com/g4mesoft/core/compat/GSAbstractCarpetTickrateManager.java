package com.g4mesoft.core.compat;

import static com.g4mesoft.core.compat.GSCarpetCompat.G4MESPEED_INTERFACE_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.ui.util.GSMathUtil;

abstract class GSAbstractCarpetTickrateManager implements GSICarpetTickrateManager {

	protected BiConsumer<String, Float> carpetTickrateListener;
	protected float lastBroadcastCarpetTickrate;
	
	private final List<GSICarpetTickrateListener> listeners;

	public GSAbstractCarpetTickrateManager() {
		lastBroadcastCarpetTickrate = GSTpsModule.DEFAULT_TPS;
		listeners = new ArrayList<>();
	}
	
	@Override
	public void addListener(GSICarpetTickrateListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null");
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(GSICarpetTickrateListener tickrateListener) {
		synchronized (listeners) {
			listeners.remove(tickrateListener);
		}
	}
	
	protected void dispatchCarpetTickrateChanged(float tickrate) {
		synchronized (listeners) {
			for (GSICarpetTickrateListener tickrateListener : listeners)
				tickrateListener.carpetTickrateChanged(tickrate);
		}
	}
	
	@Override
	public float getTickrate() {
		return lastBroadcastCarpetTickrate;
	}

	@Override
	public void setTickrate(float tickrate) {
		if (carpetTickrateListener != null && !GSMathUtil.equalsApproximate(getTickrate(), tickrate)) {
			carpetTickrateListener.accept(G4MESPEED_INTERFACE_NAME, Float.valueOf(tickrate));
			// Assume the tickrate was set correctly.
			lastBroadcastCarpetTickrate = tickrate;
		}
	}

	protected void onCarpetTickrateChanged(String modId, float tickrate) {
		if (!G4MESPEED_INTERFACE_NAME.equals(modId)) {
			lastBroadcastCarpetTickrate = tickrate;
			dispatchCarpetTickrateChanged(tickrate);
		}
	}
	
	public boolean isTickrateLinked() {
		return (carpetTickrateListener != null);
	}
}
