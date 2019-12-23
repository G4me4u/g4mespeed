package com.g4mesoft.module.tps;

import com.g4mesoft.access.GSIRenderTickCounterAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Util;

public final class GSRenderTickCounterAdjuster implements GSITpsDependant {
	
	private static final float DEFAULT_MS_PER_TICK = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;
	
	private static final float EXTRA_SERVER_SYNC_DELAY = 5.0f;
	private static final float MIN_SERVER_SYNC_DELAY = 10.0f;
	private static final float SYNC_DELAY_EASING_FACTOR = 0.25f;
	
	private static final GSRenderTickCounterAdjuster INSTANCE = new GSRenderTickCounterAdjuster();
	
	private boolean initialized = false;
	
	private float msPerTick = DEFAULT_MS_PER_TICK;
	
	private final Object serverSyncLock = new Object();
	private float approximatedServerTickDelta;
	private boolean serverSyncReceived;
	private long serverLast;
	private int serverTicksSinceLastSync;
	private int serverSyncInterval;

	private long clientLast;
	private float serverSyncDelay;
	private boolean tickAfterServer;
	
	private GSTpsModule tpsModule;
	
	private GSRenderTickCounterAdjuster() {
	}
	
	public void init(float tps, long currentMs) {
		if (!initialized) {
			serverLast = clientLast = currentMs;
			
			tpsModule = GSControllerClient.getInstance().getTpsModule();
			tpsModule.addTpsListener(this);
			
			// The only reason why we're ticking before the server is to
			// allow different animation types of pistons. But the pistons
			// can actually have pause at end whilst we are ticking after
			// the server. This allows for better looking sand animations.
			// 
			// However, it is not possible to reliably tick after the server
			// since this relies on how fast the server is running. This.
			// feature remains unused, but stays if needed in the future.
			tickAfterServer = false;

			initialized = true;
		}
	}
	
	public float getAdjustedMsPerTick(RenderTickCounter counter) {
		// Other mods such as the ReplayMod modify the
		// timeScale value of the timer. To ensure that
		// the functionality stays as expected, scale the
		// milliseconds per tick by that value.
		return msPerTick * ((GSIRenderTickCounterAccess)counter).getTickTime() / DEFAULT_MS_PER_TICK;
	}

	public void performSynchronization(RenderTickCounter counter, long currentTimeMillis) {
		synchronized (serverSyncLock) {
			updateServerClock(counter, currentTimeMillis);
			updateSyncDelay(counter, currentTimeMillis);
			
			if (tpsModule.cSyncTick.getValue() && shouldAdjustTickDelta())
				adjustTickDelta(counter);
			
			if (serverTicksSinceLastSync >= serverSyncInterval * 2)
				serverSyncReceived = false;
		}
	}
	

	private void updateServerClock(RenderTickCounter counter, long currentTimeMillis) {
		long deltaMsServer = currentTimeMillis - serverLast;
		serverLast = currentTimeMillis;
		
		if (GSControllerClient.getInstance().isG4mespeedServer()) {
			// Assume the server has the same tps
			approximatedServerTickDelta += deltaMsServer / getAdjustedMsPerTick(counter);
		} else {
			// Assume the server has DEFAULT_TPS (20) tps
			approximatedServerTickDelta += (float)deltaMsServer / DEFAULT_MS_PER_TICK;
		}
		
		int serverTicksThisFrame = (int)approximatedServerTickDelta;
		approximatedServerTickDelta -= serverTicksThisFrame;
		serverTicksSinceLastSync += serverTicksThisFrame;
	}
	
	private void updateSyncDelay(RenderTickCounter counter, long currentTimeMillis) {
		long deltaMs = currentTimeMillis - clientLast;
		clientLast = currentTimeMillis;

		float targetDelay = Math.max(MIN_SERVER_SYNC_DELAY, deltaMs + EXTRA_SERVER_SYNC_DELAY);
		serverSyncDelay += (targetDelay - serverSyncDelay) * SYNC_DELAY_EASING_FACTOR;
	}

	private boolean shouldAdjustTickDelta() {
		if (GSControllerClient.getInstance().isG4mespeedServer())
			return true;
		
		if (!serverSyncReceived)
			return false;
		
		return GSMathUtils.equalsApproximate(tpsModule.getTps(), GSTpsModule.DEFAULT_TPS);
	}
	
	private void adjustTickDelta(RenderTickCounter counter) {
		float targetTickDelta = approximatedServerTickDelta;
		if (tickAfterServer) {
			targetTickDelta -= serverSyncDelay / getAdjustedMsPerTick(counter);
		} else {
			targetTickDelta += serverSyncDelay / getAdjustedMsPerTick(counter);
		}
		
		targetTickDelta %= 1.0f;
		if (targetTickDelta < 0.0f)
			targetTickDelta++;
		
		// Check if we have to cross tick border
		// and adjust target value accordingly.
		float targetOffset = targetTickDelta - counter.tickDelta;
		if (targetOffset < -0.5f) {
			targetOffset++;
		} else if (targetOffset > 0.5f){
			targetOffset--;
		}
		
		counter.tickDelta += targetOffset * tpsModule.cSyncTickAggression.getValue();
		
		if (counter.tickDelta < 0.0f) {
			if (counter.ticksThisFrame > 0) {
				counter.ticksThisFrame--;
				counter.tickDelta++;
			} else {
				counter.tickDelta = 0.0f;
			}
		} else if (counter.tickDelta >= 1.0f) {
			counter.ticksThisFrame++;
			counter.tickDelta--;
		}
	}
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		msPerTick = GSTpsModule.MS_PER_SEC / newTps;
	}

	public void onServerTickSync(int syncInterval) {
		synchronized (serverSyncLock) {
			approximatedServerTickDelta = 0.0f;
			serverTicksSinceLastSync = 0;
			serverLast = Util.getMeasuringTimeMs();
			serverSyncInterval = syncInterval;
			serverSyncReceived = true;
		}
	}
	
	public static GSRenderTickCounterAdjuster getInstance() {
		return INSTANCE;
	}
}
