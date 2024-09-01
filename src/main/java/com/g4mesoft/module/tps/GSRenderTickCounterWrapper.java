package com.g4mesoft.module.tps;

import com.g4mesoft.access.client.GSIRenderTickCounterDynamicAccess;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Util;

public class GSRenderTickCounterWrapper extends RenderTickCounter.Dynamic implements GSITickTimer {

	private final GSITickTimer timer;
	
	public GSRenderTickCounterWrapper(GSITickTimer timer) {
		super(DEFAULT_TICKS_PER_SECOND, Util.getMeasuringTimeMs(), (ignore) -> {
			return timer.getMillisPerTick0();
		});
		this.timer = timer;
	}
	
	@Override
	public void init0(long initialTimeMillis) {
		timer.init0(initialTimeMillis);
		((GSIRenderTickCounterDynamicAccess)(Object)this).gs_setFromTimer(timer);
	}

	@Override
	public void update0(long timeMillis) {
		timer.update0(timeMillis);
		((GSIRenderTickCounterDynamicAccess)(Object)this).gs_setFromTimer(timer);
	}

	@Override
	public float getMillisPerTick0() {
		return timer.getMillisPerTick0();
	}

	@Override
	public float getTickDelta0() {
		return timer.getTickDelta0();
	}

	@Override
	public void setTickDelta0(float tickDelta) {
		timer.setTickDelta0(tickDelta);
		((GSIRenderTickCounterDynamicAccess)(Object)this).gs_setFromTimer(timer);
	}

	@Override
	public int getTickCount0() {
		return timer.getTickCount0();
	}

	@Override
	public void setTickCount0(int tickCount) {
		timer.setTickCount0(tickCount);
		((GSIRenderTickCounterDynamicAccess)(Object)this).gs_setFromTimer(timer);
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
