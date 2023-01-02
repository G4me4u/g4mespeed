package com.g4mesoft.setting.types;

import java.util.Objects;

import com.g4mesoft.setting.GSSetting;

public class GSStringSetting extends GSSetting<String> {

	private volatile String value;

	public GSStringSetting(String name, String defaultValue) {
		super(name, defaultValue, true);
	}
	
	public GSStringSetting(String name, String defaultValue, boolean visibleInGui) {
		super(name, defaultValue, visibleInGui);
		
		if (defaultValue == null)
			throw new IllegalArgumentException("GSStringSetting does not permit null values.");
		
		this.value = defaultValue;
	}
	
	@Override
	public String get() {
		return value;
	}

	@Override
	public GSStringSetting set(String value) {
		if (!value.equals(this.value)) {
			this.value = value;
			notifyOwnerChange();
		}
		
		return this;
	}
	
	@Override
	public boolean isDefault() {
		return Objects.equals(defaultValue, value);
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSStringSetting;
	}

	@Override
	public GSSetting<String> copySetting() {
		return new GSStringSetting(name, defaultValue, visibleInGui).set(value).setEnabledInGui(isEnabledInGui());
	}
}
