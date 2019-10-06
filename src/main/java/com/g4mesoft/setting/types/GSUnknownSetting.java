package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSUnknownSetting extends GSSetting<Object> {

	public GSUnknownSetting(String name) {
		super(name, null);
	}
	
	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public void setValue(Object value) {
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return false;
	}
}
