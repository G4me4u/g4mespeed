package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSBooleanSetting extends GSSetting<Boolean> {

	private boolean value;
	
	public GSBooleanSetting(String name, int identifier, boolean defaultValue) {
		super(name, identifier, defaultValue);
		
		this.value = defaultValue;
	}
	
	@Override
	public Boolean getValue() {
		return Boolean.valueOf(value);
	}

	@Override
	public void setValue(Boolean value) {
		boolean newValue = value.booleanValue();
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSBooleanSetting;
	}
}
