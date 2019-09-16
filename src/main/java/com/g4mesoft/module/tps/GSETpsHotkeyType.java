package com.g4mesoft.module.tps;

public enum GSETpsHotkeyType {

	RESET_TPS(0),
	
	INCREMENT_TPS(1),
	DECREMENT_TPS(2),
	
	DOUBLE_TPS(3),
	HALF_TPS(4);

	private static final GSETpsHotkeyType[] TYPES;
	
	static {
		TYPES = new GSETpsHotkeyType[values().length];
		for (GSETpsHotkeyType type : values())
			TYPES[type.index] = type;
	}
	
	private final int index;
	
	private GSETpsHotkeyType(int index) {
		this.index = index;
	}
	
	public static GSETpsHotkeyType fromIndex(int index) {
		if (index < 0 || index >= TYPES.length)
			return null;
		return TYPES[index];
	}

	public int getIndex() {
		return index;
	}
}
