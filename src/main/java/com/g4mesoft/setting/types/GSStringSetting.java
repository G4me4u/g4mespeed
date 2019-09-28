package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSStringSetting extends GSSetting<String> {

	private String value;
	
	public GSStringSetting(String name, int identifier, String defaultValue) {
		super(name, identifier, defaultValue);
		
		this.value = defaultValue;
	}
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		if (!value.equals(this.value)) {
			this.value = value;
			notifyOwnerChange();
		}
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSStringSetting;
	}
}
