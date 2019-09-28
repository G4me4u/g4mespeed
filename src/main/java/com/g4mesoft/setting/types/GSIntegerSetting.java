package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSIntegerSetting extends GSSetting<Integer> {

	private int value;
	
	public GSIntegerSetting(String name, int identifier, int defaultValue) {
		super(name, identifier, defaultValue);
		
		this.value = defaultValue;
	}
	
	@Override
	public Integer getValue() {
		return Integer.valueOf(value);
	}

	@Override
	public void setValue(Integer value) {
		int newValue = value.intValue();
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSIntegerSetting;
	}
}
