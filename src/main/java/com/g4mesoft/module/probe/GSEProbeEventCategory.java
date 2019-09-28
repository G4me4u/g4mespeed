package com.g4mesoft.module.probe;

public enum GSEProbeEventCategory {

	ALL(0, "all"),
	BLOCK_EVENT(1, "blockEvent"),
	SCHEDULED_TICK(2, "scheduledTick"),
	REDSTONE_POWER(3, "redstonePower");

	private static final GSEProbeEventCategory[] PROBE_TYPES;
	
	static {
		PROBE_TYPES = new GSEProbeEventCategory[values().length];
		for (GSEProbeEventCategory type : values()) {
			if (PROBE_TYPES[type.index] != null)
				throw new ExceptionInInitializerError("Duplicate probe category index");
			PROBE_TYPES[type.index] = type;
		}
	}
	
	private final int index;
	private final String name;
	
	private GSEProbeEventCategory(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static GSEProbeEventCategory fromIndex(int index) {
		if (index < 0 || index >= PROBE_TYPES.length)
			return null;
		return PROBE_TYPES[index];
	}
	
	public String getName() {
		return name;
	}
}
