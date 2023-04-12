package com.g4mesoft.setting.types;

import com.g4mesoft.setting.GSSetting;

public class GSBooleanSetting extends GSSetting<Boolean> {

	private volatile boolean value;

	public GSBooleanSetting(String name, boolean defaultValue) {
		this(name, defaultValue, true);
	}
	
	public GSBooleanSetting(String name, boolean defaultValue, boolean visibleInGui) {
		super(name, defaultValue, visibleInGui);
		
		this.value = defaultValue;
	}
	
	@Override
	public Boolean get() {
		return Boolean.valueOf(value);
	}
	
	/**
	 * @deprecated Replaced by {@link #get()}
	 * 
	 * @return the value of this setting
	 */
	@Deprecated
	public Boolean getValue() {
		return get();
	}

	@Override
	public GSBooleanSetting set(Boolean value) {
		return set(value.booleanValue());
	}
	
	/**
<<<<<<< HEAD
	 * @deprecated Replaced by {@link #set(Object)}
=======
	 * @deprecated Replaced by {@link #set(Boolean)}
>>>>>>> 9c5d50f8c0adf045472579d7cc59d5268c81cbfd
	 * 
	 * @param value - the new value of this setting
	 * 
	 * @return this setting
	 */
	@Deprecated
	public GSBooleanSetting setValue(Boolean value) {
		return set(value);
	}
	
	/**
	 * Sets the boolean value in this setting to the value specified.
	 * 
	 * @param value - the new value to be stored in this setting
	 * 
	 * @return this boolean setting.
	 */
	public GSBooleanSetting set(boolean value) {
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
		return set(!value);
	}
	
	@Override
	public boolean isDefault() {
		return defaultValue.booleanValue() == value;
	}

	@Override
	public boolean isSameType(GSSetting<?> other) {
		return other instanceof GSBooleanSetting;
	}

	@Override
	public GSSetting<Boolean> copySetting() {
		return new GSBooleanSetting(name, defaultValue, visibleInGui).set(value).setEnabledInGui(isEnabledInGui());
	}
}
