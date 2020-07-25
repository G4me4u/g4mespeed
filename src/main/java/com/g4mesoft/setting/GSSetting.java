package com.g4mesoft.setting;

public abstract class GSSetting<T> {

	protected final String name;
	protected final T defaultValue;
	protected final boolean visibleInGui;
	
	private GSSettingMap settingOwner;
	private boolean active;
	
	private boolean enabledInGui;
	
	public GSSetting(String name, T defaultValue, boolean visibleInGui) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.visibleInGui = visibleInGui;
		
		settingOwner = null;
		active = true;
		
		enabledInGui = true;
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
	
	public abstract T getValue();
	
	public abstract GSSetting<T> setValue(T value);

	public abstract boolean isDefaultValue();

	public abstract boolean isSameType(GSSetting<?> other);

	public abstract GSSetting<T> copySetting();

	public void reset() {
		setValue(defaultValue);
	}
	
	void setValueIfSameType(GSSetting<?> other) {
		if (isSameType(other)) {
			@SuppressWarnings("unchecked")
			T otherValue = (T)other.getValue();
			setValue(otherValue);
		}
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}

	void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isVisibleInGUI() {
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
}
