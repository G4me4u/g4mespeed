package com.g4mesoft.module.tps;

import java.util.Arrays;

import net.minecraft.util.Util;

public class GSTpsMonitor {

	private static final long MILLIS_PER_SECOND = 1000;
	private static final int MAX_TPS_HISTORY_SIZE = 10;
	
	private final int[] tpsHistory;
	private int tpsHistorySize;
	private int tpsHistoryPosition;
	private long tpsAccumulator;

	private long lastUpdateTime;
	private int ticks;

	private volatile float averageTps;
	
	public GSTpsMonitor() {
		tpsHistory = new int[MAX_TPS_HISTORY_SIZE];

		reset();
	}
	
	public synchronized void reset() {
		Arrays.fill(tpsHistory, 0);
		tpsHistorySize = 0;
		tpsHistoryPosition = 0;
		tpsAccumulator = 0L;
		
		lastUpdateTime = Util.getMeasuringTimeMs();
		ticks = 1;
	}
	
	public synchronized void update(int ticksPassed) {
		ticks += ticksPassed;
		
		long millisPassed = Util.getMeasuringTimeMs() - lastUpdateTime;
		long secondsPassed = millisPassed / MILLIS_PER_SECOND;
		if (secondsPassed > 0L) {
			long millisThisUpdate = secondsPassed * MILLIS_PER_SECOND;
			int ticksThisUpdate = (int)(ticks * millisThisUpdate / millisPassed);

			if (ticksThisUpdate < 0) {
				// In the special case where overflow occurs, we should not
				// have negative ticks. Note that this can really only happen
				// if the system time changes drastically.
				ticksThisUpdate = ticks;
			}
			
			lastUpdateTime += millisThisUpdate;
			ticks -= ticksThisUpdate;

			if (secondsPassed > MAX_TPS_HISTORY_SIZE) {
				long oldSeconds = (secondsPassed - MAX_TPS_HISTORY_SIZE);
				int oldTicks = (int)(ticksThisUpdate * oldSeconds / secondsPassed);
				if (oldTicks > 0) {
					// Again to handle overflow.
					ticksThisUpdate -= oldTicks;
				}
				secondsPassed = MAX_TPS_HISTORY_SIZE;
			}
			
			int secondsToProcess = (int)secondsPassed;
			for (; secondsToProcess > 0; secondsToProcess--) {
				int ticksThisSecond = ticksThisUpdate / secondsToProcess;
				addToHistory(ticksThisSecond);
				ticksThisUpdate -= ticksThisSecond;
			}
			
			averageTps = (float)((double)tpsAccumulator / tpsHistorySize);
		}
	}
	
	private void addToHistory(int tps) {
		tpsAccumulator -= tpsHistory[tpsHistoryPosition];
		tpsHistory[tpsHistoryPosition] = tps;
		tpsAccumulator += tps;
	
		if (++tpsHistoryPosition >= MAX_TPS_HISTORY_SIZE)
			tpsHistoryPosition = 0;
		if (tpsHistorySize < MAX_TPS_HISTORY_SIZE)
			tpsHistorySize++;
	}
	
	public float getAverageTps() {
		return averageTps;
	}
}
