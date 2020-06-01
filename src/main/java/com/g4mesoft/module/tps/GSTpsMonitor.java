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

	private boolean firstUpdate;
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
		
		firstUpdate = true;
		lastUpdateTime = -1L;
		
		ticks = 0;

		averageTps = 0.0f;
	}
	
	public synchronized void update(int ticksPassed) {
		long now = Util.getMeasuringTimeMs();
		if (firstUpdate) {
			firstUpdate = false;
			lastUpdateTime = now;
		}

		long secondsPassed = (now - lastUpdateTime) / MILLIS_PER_SECOND;
		if (secondsPassed > 0L) {
			lastUpdateTime += secondsPassed * MILLIS_PER_SECOND;

			if (secondsPassed > MAX_TPS_HISTORY_SIZE) {
				Arrays.fill(tpsHistory, 0);
				tpsHistorySize = MAX_TPS_HISTORY_SIZE;
				tpsHistoryPosition = 0;
				tpsAccumulator = 0L;
				
				averageTps = 0.0f;
			} else {
				addToHistory(ticks);
				ticks = 0;
				
				// Fill in the rest of the positions with zeros
				while (--secondsPassed > 0L)
					addToHistory(0);
				
				averageTps = (float)((double)tpsAccumulator / tpsHistorySize);
			}
		}
		
		ticks += ticksPassed;
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
