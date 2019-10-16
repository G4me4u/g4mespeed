package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSBooleanSetting extends GSSetting<Boolean> {

	private boolean value;

	public GSBooleanSetting(String name, boolean defaultValue) {
		this(name, defaultValue, true);
	}
	
	public GSBooleanSetting(String name, boolean defaultValue, boolean availableInGui) {
		super(name, defaultValue, availableInGui);
		
		this.value = defaultValue;
	}
	
	@Override
	public Boolean getValue() {
		return Boolean.valueOf(value);
	}

	@Override
	public GSBooleanSetting setValue(Boolean value) {
		boolean newValue = value.booleanValue();
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
		
		return this;
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSBooleanSetting;
	}

	@Override
	public GSSetting<Boolean> copySetting() {
		return new GSBooleanSetting(name, defaultValue, availableInGui).setValue(value);
	}
}
