package com.g4mesoft.module.tps;

import net.minecraft.client.render.RenderTickCounter;

public class GSRenderTickCounterWrapper implements GSITickTimer, RenderTickCounter {

	private final GSITickTimer timer;
	
	public GSRenderTickCounterWrapper(GSITickTimer timer) {
		this.timer = timer;
	}
	
	@Override
	public void init0(long initialTimeMillis) {
		timer.init0(initialTimeMillis);
	}

	@Override
	public void update0(long timeMillis) {
		timer.update0(timeMillis);
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
	}

	@Override
	public int getTickCount0() {
		return timer.getTickCount0();
	}

	@Override
	public void setTickCount0(int tickCount) {
		timer.setTickCount0(tickCount);
	}

	@Override
	public float getLastDuration0() {
		return timer.getLastDuration0();
	}
	
	@Override
	public float getLastFrameDuration() {
		return timer.getLastDuration0();
	}

	@Override
	public float getTickDelta(boolean var1) {
		return timer.getTickDelta0();
	}

	@Override
	public float getLastDuration() {
		return timer.getLastDuration0();
	}
}
