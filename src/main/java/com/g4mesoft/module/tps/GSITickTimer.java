package com.g4mesoft.module.tps;

public interface GSITickTimer {

	public static final float MILLIS_PER_SECOND        = 1000.0f;
	public static final float DEFAULT_MILLIS_PER_TICK  = 50.0f;
	public static final float DEFAULT_TICKS_PER_SECOND = MILLIS_PER_SECOND / DEFAULT_MILLIS_PER_TICK;

	/* Methods have zero appended for compatibility reasons (mojmap) */
	
	public void init0(long initialTimeMillis);
	
	public void update0(long timeMillis);

	public float getMillisPerTick0();
	
	public float getTickDelta0();
	
	public void setTickDelta0(float tickDelta);
	
	public int getTickCount0();

	public void setTickCount0(int tickCount);
	
}
