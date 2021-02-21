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
	public void init(long initialTimeMillis) {
		prevTimeMillis = initialTimeMillis;
		tickDelta = 0.0f;
	}
	
	@Override
	public void update(long timeMillis) {
		long deltaMillis = timeMillis - prevTimeMillis;
		prevTimeMillis = timeMillis;
		
		tickDelta += deltaMillis / millisPerTick;
		tickCount = (int)tickDelta;
		tickDelta -= tickCount;
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
}
