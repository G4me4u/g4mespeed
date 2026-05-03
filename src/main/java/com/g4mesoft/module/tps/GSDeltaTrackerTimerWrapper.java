package com.g4mesoft.module.tps;

import com.g4mesoft.access.client.GSIDeltaTrackerTimerAccess;

import net.minecraft.client.DeltaTracker;
import net.minecraft.util.Util;

public class GSDeltaTrackerTimerWrapper extends DeltaTracker.Timer implements GSITickTimer {

	private final GSITickTimer timer;
	
	public GSDeltaTrackerTimerWrapper(GSITickTimer timer) {
		super(DEFAULT_TICKS_PER_SECOND, Util.getMillis(), (ignore) -> {
			return timer.getMillisPerTick0();
		});
		this.timer = timer;
	}
	
	@Override
	public void init0(long initialTimeMillis) {
		timer.init0(initialTimeMillis);
		((GSIDeltaTrackerTimerAccess)(Object)this).gs_setFromTimer(timer);
	}

	@Override
	public void update0(long timeMillis) {
		timer.update0(timeMillis);
		((GSIDeltaTrackerTimerAccess)(Object)this).gs_setFromTimer(timer);
	}

	@Override
	public float getMillisPerTick0() {
		return timer.getMillisPerTick0();
	}

	@Override
	public float getPartialTick0() {
		return timer.getPartialTick0();
	}

	@Override
	public void setPartialTick0(float tickDelta) {
		timer.setPartialTick0(tickDelta);
		((GSIDeltaTrackerTimerAccess)(Object)this).gs_setFromTimer(timer);
	}

	@Override
	public int getTickCount0() {
		return timer.getTickCount0();
	}

	@Override
	public void setTickCount0(int tickCount) {
		timer.setTickCount0(tickCount);
		((GSIDeltaTrackerTimerAccess)(Object)this).gs_setFromTimer(timer);
	}

	@Override
	public float getLastDuration0() {
		return timer.getLastDuration0();
	}

	@Override
	public long getPrevTimeMillis0() {
		return timer.getPrevTimeMillis0();
	}
}
