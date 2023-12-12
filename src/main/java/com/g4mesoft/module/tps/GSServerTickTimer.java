package com.g4mesoft.module.tps;

import com.g4mesoft.core.client.GSClientController;

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
	public synchronized void init0(long initialTimeMillis) {
		prevTimeMillis = initialTimeMillis;
		tickDelta = 0.0f;
		ticksSinceLastPacket = 0;
		tickCount = 0;
	}
	
	@Override
	public synchronized void update0(long timeMillis) {
		long deltaMillis = timeMillis - prevTimeMillis;
		prevTimeMillis = timeMillis;
		
		tickDelta += deltaMillis / getMillisPerTick0();
		
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
	public float getMillisPerTick0() {
		return millisPerTick;
	}
	
	public void setMillisPerTick(float millisPerTick) {
		this.millisPerTick = millisPerTick;
	}

	@Override
	public synchronized float getTickDelta0() {
		return tickDelta;
	}

	@Override
	public synchronized void setTickDelta0(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	@Override
	public synchronized int getTickCount0() {
		return tickCount;
	}
	
	@Override
	public synchronized void setTickCount0(int tickCount) {
		this.tickCount = tickCount;
	}
	
	public synchronized void syncTimer(GSITickTimer timer) {
		if (tpsModule.cSyncTick.get() && shouldAdjustTickDelta())
			adjustTickDelta(timer);
	}
	
	private boolean shouldAdjustTickDelta() {
		if (GSClientController.getInstance().isG4mespeedServer()) {
			// Sync if the server is NOT freezing/stepping/sprinting.
			return !tpsModule.isFrozen() && !tpsModule.isStepping() && !tpsModule.isSprinting();
		}
		return syncReceived && tpsModule.isSameTpsAsServer();
	}

	private void adjustTickDelta(GSITickTimer timer) {
		float targetTickDelta = tickDelta + syncDelay / timer.getMillisPerTick0();
		
		targetTickDelta %= 1.0f;
		if (targetTickDelta < 0.0f)
			targetTickDelta++;
		
		float syncTickDelta = timer.getTickDelta0();
		int syncTickCount = timer.getTickCount0();
		
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
		
		timer.setTickDelta0(syncTickDelta);
		timer.setTickCount0(syncTickCount);
	}
	
	public synchronized void onSyncPacket(int syncTickInterval) {
		this.syncTickInterval = syncTickInterval;
		syncReceived = true;

		init0(Util.getMeasuringTimeMs());
	}
}
