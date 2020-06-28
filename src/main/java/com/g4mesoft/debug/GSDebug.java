package com.g4mesoft.debug;

public class GSDebug {

	public static final boolean GS_DEBUG = false;
	
	private static final boolean PRINT_TICK_INTERVAL = false;
	
	public static void onClientTick() {
		if (!GS_DEBUG)
			return;
		
		if (PRINT_TICK_INTERVAL)
			printTimeIntervalSync("Client");
	}

	public static void onServerTick() {
		if (!GS_DEBUG)
			return;
		
		if (PRINT_TICK_INTERVAL)
			printTimeIntervalSync("Server");
	}

	private static long lastMethodCallTimeNs = System.nanoTime();
	
	private static synchronized void printTimeIntervalSync(String sideName) {
		long methodCallTimeNs = System.nanoTime();
		System.out.println(String.format("%s: %.2fms", sideName, (float)(methodCallTimeNs - lastMethodCallTimeNs) / 1e6f));
		lastMethodCallTimeNs = methodCallTimeNs;
	}
}
