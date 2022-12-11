package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSBooleanSetting extends GSSetting<Boolean> {

	private boolean value;

	public GSBooleanSetting(String name, boolean defaultValue) {
		this(name, defaultValue, true);
	}
	
	public GSBooleanSetting(String name, boolean defaultValue, boolean visibleInGui) {
		super(name, defaultValue, visibleInGui);
		
		this.value = defaultValue;
	}
	
	@Override
	public Boolean getValue() {
		return Boolean.valueOf(value);
	}

	@Override
	public GSBooleanSetting setValue(Boolean value) {
		return setValue(value.booleanValue());
	}
	
	/**
	 * Sets the boolean value in this setting to the value specified.
	 * 
	 * @param value - the new value to be stored in this setting
	 * 
	 * @return this boolean setting.
	 */
	public GSBooleanSetting setValue(boolean value) {
		if (value != this.value) {
			this.value = value;
			notifyOwnerChange();
		}
		
		return this;
	}

	/**
	 * Toggles this boolean setting. That is, if the currently stored value is
	 * {@code true} then the value after an invocation is {@code false}, and
	 * vice versa. An invocation of this method is equivalent to the following
	 * code snippet:
	 * <pre>
	 *     setting.setValue(!setting.getValue());
	 * </pre>
	 * 
	 * @return this boolean setting
	 * 
	 * @see #setValue(boolean)
	 */
	public GSBooleanSetting toggle() {
		return setValue(!value);
	}
	
	@Override
	public boolean isDefaultValue() {
		return defaultValue.booleanValue() == value;
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSBooleanSetting;
	}

	@Override
	public GSSetting<Boolean> copySetting() {
		return new GSBooleanSetting(name, defaultValue, visibleInGui).setValue(value).setEnabledInGui(isEnabledInGui());
	}
}
