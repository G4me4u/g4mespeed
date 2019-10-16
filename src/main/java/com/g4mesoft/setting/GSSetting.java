package com.g4mesoft.setting;

public abstract class GSSetting<T> {

	protected final String name;
	protected final T defaultValue;
	protected final boolean availableInGui;
	
	private GSSettingMap settingOwner;
	private boolean active;
	
	public GSSetting(String name, T defaultValue, boolean availableInGui) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.availableInGui = availableInGui;
		
		settingOwner = null;
		active = true;
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

	public abstract boolean isSameType(GSSetting<?> other);

	public abstract GSSetting<T> copySetting();

	public void reset() {
		setValue(defaultValue);
	}
	
	@SuppressWarnings("unchecked")
	public void setValueIfSameType(GSSetting<?> other) {
		if (isSameType(other))
			setValue((T)other.getValue());
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
	
	public boolean isAvailableInGUI() {
		return availableInGui;
	}
}
