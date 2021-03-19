package com.g4mesoft.module.tps;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.util.Util;

public class GSServerTickTimer implements GSITickTimer {

	private static final float EXTRA_SYNC_DELAY = 5.0f;
	private static final float MIN_SYNC_DELAY = 10.0f;
	private static final float SYNC_DELAY_EASING_FACTOR = 0.25f;
	private static final float SYNC_AGGRESSION = 0.05f;
	
	private final GSTpsModule tpsModule;
	
	private float millisPerTick;
	private long prevTimeMillis;
	private float tickDelta;
	private int tickCount;
	private int ticksSinceLastPacket;
	
	private int syncTickInterval;
	private boolean syncReceived;
	private float syncDelay;
	
	public GSServerTickTimer(GSTpsModule tpsModule) {
		this.tpsModule = tpsModule;
	
		millisPerTick = DEFAULT_MILLIS_PER_TICK;
	}
	
	@Override
	public void init(long initialTimeMillis) {
		prevTimeMillis = initialTimeMillis;
		tickDelta = 0.0f;
		ticksSinceLastPacket = 0;
		tickCount = 0;
	}

	@Override
	public synchronized void update(long timeMillis) {
		long deltaMillis = timeMillis - prevTimeMillis;
		prevTimeMillis = timeMillis;
		
		tickDelta += deltaMillis / getMillisPerTick();
		
		tickCount = (int)tickDelta;
		tickDelta -= tickCount;
		ticksSinceLastPacket += tickCount;

		if (ticksSinceLastPacket >= syncTickInterval * 2)
			syncReceived = false;
		
		updateSyncDelay(deltaMillis);
	}
	
	private void updateSyncDelay(long deltaMillis) {
		float targetDelay = Math.max(MIN_SYNC_DELAY, deltaMillis + EXTRA_SYNC_DELAY);
		syncDelay += (targetDelay - syncDelay) * SYNC_DELAY_EASING_FACTOR;
	}

	@Override
	public float getMillisPerTick() {
		return millisPerTick;
	}
	
	public void setMillisPerTick(float millisPerTick) {
		this.millisPerTick = millisPerTick;
	}

	@Override
	public float getTickDelta() {
		return tickDelta;
	}

	@Override
	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	@Override
	public int getTickCount() {
		return tickCount;
	}
	
	@Override
	public void setTickCount(int tickCount) {
		this.tickCount = tickCount;
	}
	
	public void syncTimer(GSITickTimer timer) {
		if (tpsModule.cSyncTick.getValue() && shouldAdjustTickDelta())
			adjustTickDelta(timer);
	}
	
	private boolean shouldAdjustTickDelta() {
		if (GSControllerClient.getInstance().isG4mespeedServer()) {
			// When Fabric Carpet tickrate is linked, it is possible
			// to use their client tickrate. Make sure to only enforce
			// synchronization when using G4mespeed tickrate.
			GSCarpetCompat carpetCompat = G4mespeedMod.getInstance().getCarpetCompat();
			if (!carpetCompat.isTickrateLinked() || tpsModule.cForceCarpetTickrate.getValue())
				return true;
		}

		if (!syncReceived)
			return false;
		
		return GSMathUtils.equalsApproximate(tpsModule.getTps(), DEFAULT_TICKS_PER_SECOND);
	}

	private void adjustTickDelta(GSITickTimer timer) {
		float targetTickDelta = tickDelta + syncDelay / timer.getMillisPerTick();
		
		targetTickDelta %= 1.0f;
		if (targetTickDelta < 0.0f)
			targetTickDelta++;
		
		float syncTickDelta = timer.getTickDelta();
		int syncTickCount = timer.getTickCount();
		
		// Check if we have to cross tick border
		// and adjust target value accordingly.
		float targetOffset = targetTickDelta - syncTickDelta;
		if (targetOffset < -0.5f) {
			targetOffset++;
		} else if (targetOffset > 0.5f){
			targetOffset--;
		}
		
		syncTickDelta += targetOffset * SYNC_AGGRESSION;
		
		if (syncTickDelta < 0.0f) {
			if (syncTickCount > 0) {
				syncTickCount--;
				syncTickDelta++;
			} else {
				syncTickDelta = 0.0f;
			}
		} else if (syncTickDelta >= 1.0f) {
			syncTickCount++;
			syncTickDelta--;
		}
		
		timer.setTickDelta(syncTickDelta);
		timer.setTickCount(syncTickCount);
	}
	
	public synchronized void onSyncPacket(int syncTickInterval) {
		this.syncTickInterval = syncTickInterval;
		syncReceived = true;

		init(Util.getMeasuringTimeMs());
	}

}
