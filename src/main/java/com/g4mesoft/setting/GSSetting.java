package com.g4mesoft.setting;

import java.util.Objects;

public abstract class GSSetting<T> {

	protected final String name;
	protected final T defaultValue;
	protected final boolean visibleInGui;
	
	private GSSettingMap settingOwner;
	private boolean active;
	
	private boolean enabledInGui;
	private boolean allowedChange;
	
	public GSSetting(String name, T defaultValue, boolean visibleInGui) {
		if (name == null)
			throw new IllegalArgumentException("name is null");
		
		this.name = name;
		this.defaultValue = defaultValue;
		this.visibleInGui = visibleInGui;
		
		settingOwner = null;
		active = true;
		
		enabledInGui = true;
		allowedChange = true;
	}
	
	public String getName() {
		return name;
	}
	
	void setSettingOwner(GSSettingMap changeListener) {
		if (changeListener != null && this.settingOwner != null)
			throw new IllegalStateException("Change listener already set!");
		this.settingOwner = changeListener;
	}

	protected void notifyOwnerChange() {
		if (settingOwner != null)
			settingOwner.settingChanged(this);
	}
	
	/**
	 * @return the value of this setting
	 */
	public abstract T get();
	
	/**
	 * @param value - the new value of this setting
	 * 
	 * @return this setting
	 */
	public abstract GSSetting<T> set(T value);

	/**
	 * @return True iff. the value of this setting is considered equal to
	 *         that returned by {@link #getDefault()}. False otherwise.
	 */
	public abstract boolean isDefault();

	public abstract boolean isSameType(GSSetting<?> other);

	public boolean isSameSetting(GSSetting<?> other) {
		return isSameType(other) && Objects.equals(defaultValue, other.getDefault());
	}
	
	public abstract GSSetting<T> copySetting();

	public void reset() {
		set(defaultValue);
	}
	
	void setIfSameType(GSSetting<?> other) {
		if (isSameType(other)) {
			@SuppressWarnings("unchecked")
			T otherValue = (T)other.get();
			set(otherValue);
		}
	}
	
	/**
	 * @return the default value
	 */
	public T getDefault() {
		return defaultValue;
	}

	void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isVisibleInGui() {
		return visibleInGui;
	}

	public GSSetting<T> setEnabledInGui(boolean enabledInGui) {
		if (enabledInGui != this.enabledInGui) {
			this.enabledInGui = enabledInGui;
			notifyOwnerChange();
		}
		return this;
	}

	public boolean isEnabledInGui() {
		return enabledInGui;
	}

	public GSSetting<T> setAllowedChange(boolean allowedChange) {
		if (allowedChange != this.allowedChange) {
			this.allowedChange = allowedChange;
			notifyOwnerChange();
		}
		return this;
	}
	
	public boolean isAllowedChange() {
		return allowedChange;
	}
}
