package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSStringSetting extends GSSetting<String> {

	private String value;

	public GSStringSetting(String name, String defaultValue) {
		super(name, defaultValue, true);
	}
	
	public GSStringSetting(String name, String defaultValue, boolean availableInGui) {
		super(name, defaultValue, availableInGui);
		
		this.value = defaultValue;
	}
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public GSStringSetting setValue(String value) {
		if (!value.equals(this.value)) {
			this.value = value;
			notifyOwnerChange();
		}
		
		return this;
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSStringSetting;
	}

	@Override
	public GSSetting<String> copySetting() {
		return new GSStringSetting(name, defaultValue, availableInGui).setValue(value);
	}
}
