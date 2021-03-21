package com.g4mesoft.debug;

public class GSDebug {

	public static final boolean GS_DEBUG = false;
	private static final boolean PRINT_TICK_INTERVAL = false;

	private static long lastServerMethodCallNs = System.nanoTime();
	/* Volatile to ensure the cache is invalidated for the server thread */
	private static volatile long lastClientMethodCallNs = System.nanoTime();
	private static long printTimerNs;
	private static long lowDeltaNs;
	private static long highDeltaNs;
	
	public static void onClientTick() {
		if (!GS_DEBUG)
			return;
		
		if (PRINT_TICK_INTERVAL)
			lastClientMethodCallNs = System.nanoTime();
	}

	public static void onServerTick() {
		if (!GS_DEBUG)
			return;

		if (PRINT_TICK_INTERVAL)
			printTimeIntervalSync();
	}

	/* Only call from server thread! */
	private static void printTimeIntervalSync() {
		long methodCallNs = System.nanoTime();
		
		printTimerNs += methodCallNs - lastServerMethodCallNs;
		lastServerMethodCallNs = methodCallNs;

		long deltaNs = methodCallNs - lastClientMethodCallNs;
		lowDeltaNs = Math.min(lowDeltaNs, deltaNs);
		highDeltaNs = Math.max(highDeltaNs, deltaNs);
		
		if (printTimerNs > 1000000000L) {
			System.out.println("Tick Cycle Status: ");
			System.out.println(String.format("Low  (server - client): %.2fms", (float)lowDeltaNs / 1e6f));
			System.out.println(String.format("High (server - client): %.2fms", (float)highDeltaNs / 1e6f));
			lowDeltaNs = Long.MAX_VALUE;
			highDeltaNs = Long.MIN_VALUE;
			printTimerNs = 0;
		}
	}
}
