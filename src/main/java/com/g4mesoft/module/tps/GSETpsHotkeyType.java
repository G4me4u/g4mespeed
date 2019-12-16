package com.g4mesoft.module.tps;

public enum GSETpsHotkeyType {

	RESET_TPS(0),
	
	INCREMENT_TPS(1),
	DECREMENT_TPS(2),
	
	DOUBLE_TPS(3),
	HALVE_TPS(4);

	private static final GSETpsHotkeyType[] HOTKEY_TYPES;
	
	static {
		HOTKEY_TYPES = new GSETpsHotkeyType[values().length];
		for (GSETpsHotkeyType type : values()) {
			if (HOTKEY_TYPES[type.index] != null)
				throw new ExceptionInInitializerError("Duplicate hotkey index");
			HOTKEY_TYPES[type.index] = type;
		}
	}
	
	private final int index;
	
	private GSETpsHotkeyType(int index) {
		this.index = index;
	}
	
	public static GSETpsHotkeyType fromIndex(int index) {
		if (index < 0 || index >= HOTKEY_TYPES.length)
			return null;
		return HOTKEY_TYPES[index];
	}

	public int getIndex() {
		return index;
	}
}
