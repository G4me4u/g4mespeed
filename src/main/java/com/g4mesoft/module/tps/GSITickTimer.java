package com.g4mesoft.module.tps;

public interface GSITickTimer {

	public static final float MILLIS_PER_SECOND        = 1000.0f;
	public static final float DEFAULT_MILLIS_PER_TICK  = 50.0f;
	public static final float DEFAULT_TICKS_PER_SECOND = MILLIS_PER_SECOND / DEFAULT_MILLIS_PER_TICK;
	
	public void init(long initialTimeMillis);
	
	public void update(long timeMillis);

	public float getMillisPerTick();
	
	public float getTickDelta();
	
	public void setTickDelta(float tickDelta);
	
	public int getTickCount();

	public void setTickCount(int tickCount);
	
}
