package com.g4mesoft.module.tps;

public class GSBasicTickTimer implements GSITickTimer {

	private float millisPerTick;

	private long prevTimeMillis;
	private float tickDelta;
	private int tickCount;

	public GSBasicTickTimer(float millisPerTick) {
		this.millisPerTick = millisPerTick;
	}
	
	@Override
	public void init0(long initialTimeMillis) {
		prevTimeMillis = initialTimeMillis;
		tickDelta = 0.0f;
	}
	
	@Override
	public void update0(long timeMillis) {
		long deltaMillis = timeMillis - prevTimeMillis;
		prevTimeMillis = timeMillis;
		
		tickDelta += deltaMillis / millisPerTick;
		tickCount = (int)tickDelta;
		tickDelta -= tickCount;
	}

	@Override
	public float getMillisPerTick0() {
		return millisPerTick;
	}
	
	public void setMillisPerTick(float millisPerTick) {
		this.millisPerTick = millisPerTick;
	}
	
	@Override
	public float getTickDelta0() {
		return tickDelta;
	}
	
	@Override
	public void setTickDelta0(float tickDelta) {
		this.tickDelta = tickDelta;
	}
	
	@Override
	public int getTickCount0() {
		return tickCount;
	}
	
	@Override
	public void setTickCount0(int tickCount) {
		this.tickCount = tickCount;
	}
}
