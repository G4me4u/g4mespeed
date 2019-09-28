package com.g4mesoft.module.probe;

import static com.g4mesoft.module.probe.GSEProbeEventCategory.BLOCK_EVENT;
import static com.g4mesoft.module.probe.GSEProbeEventCategory.SCHEDULED_TICK;
import static com.g4mesoft.module.probe.GSEProbeEventCategory.REDSTONE_POWER;

import static com.g4mesoft.module.probe.GSProbeModule.PROBE_INTRODUCTION_VERSION;

public enum GSEProbeEventType {

	BLOCK_POWER(0, "blockPower", REDSTONE_POWER, PROBE_INTRODUCTION_VERSION),
	BLOCK_DEPOWER(1, "blockDepower", REDSTONE_POWER, PROBE_INTRODUCTION_VERSION),
	
	DUST_SS_CHANGE(2, "dustSSChange", REDSTONE_POWER, PROBE_INTRODUCTION_VERSION),
	
	PISTON_EXTEND(3, "pistonExtend", BLOCK_EVENT, PROBE_INTRODUCTION_VERSION),
	PISTON_RETRACT(4, "pistonRetract", BLOCK_EVENT, PROBE_INTRODUCTION_VERSION),
	
	REPEATER_TURN_ON(5, "repeaterOn", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	REPEATER_TURN_OFF(6, "repeaterOff", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	REPEATER_LOCK_CHANGE(7, "repeaterLock", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	
	REDSTONE_TORCH_TURN_ON(8, "torchOn", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	REDSTONE_TORCH_TURN_OFF(9, "torchOff", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	REDSTONE_TORCH_BURNOUT(9, "torchBurnout", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	
	COMPARATOR_TURN_ON(10, "comparatorOn", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	COMPARATOR_TURN_OFF(11, "comparatorOff", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	COMPARATOR_SIDE_CHANGE(12, "comparatorSide", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	COMPARATOR_POWER_CHANGE(13, "comparatorPower", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	
	OBSERVER_TURN_ON(14, "observerOn", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	OBSERVER_TURN_OFF(15, "observerOff", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	OBSERVER_DETECT(16, "observerDetect", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),

	DISPENSER_TURN_ON(17, "dispenserOn", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION),
	DISPENSER_TURN_OFF(18, "dispenserOff", SCHEDULED_TICK, PROBE_INTRODUCTION_VERSION);

	private static final GSEProbeEventType[] EVENT_TYPES;
	
	static {
		EVENT_TYPES = new GSEProbeEventType[values().length];
		for (GSEProbeEventType type : values()) {
			if (EVENT_TYPES[type.index] != null)
				throw new ExceptionInInitializerError("Duplicate event type index");
			EVENT_TYPES[type.index] = type;
		}
	}
	
	private final int index;
	private final String name;
	private final GSEProbeEventCategory category;
	private final int versionAdded;

	private GSEProbeEventType(int index, String name, GSEProbeEventCategory category, int versionAdded) {
		this.index = index;
		this.name = name;
		this.category = category;
		this.versionAdded = versionAdded;
	}
	
	public int getIndex() {
		return index;
	}

	public static GSEProbeEventType fromIndex(int index) {
		if (index < 0 || index >= EVENT_TYPES.length)
			return null;
		return EVENT_TYPES[index];
	}
	
	public String getName() {
		return name;
	}
	
	public GSEProbeEventCategory getCategory() {
		return category;
	}
	
	public int getVersionAdded() {
		return versionAdded;
	}
}
