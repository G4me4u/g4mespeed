package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSFloatSetting extends GSSetting<Float> {

	private float value;
	
	public GSFloatSetting(String name, int identifier, float defaultValue) {
		super(name, identifier, defaultValue);

		this.value = defaultValue;
	}
	
	@Override
	public Float getValue() {
		return Float.valueOf(value);
	}

	@Override
	public void setValue(Float value) {
		float newValue = value.floatValue();
		if (newValue != this.value) {
			this.value = newValue;
			notifyOwnerChange();
		}
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSFloatSetting;
	}
}
