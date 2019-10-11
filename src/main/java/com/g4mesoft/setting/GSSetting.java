package com.g4mesoft.setting;

public abstract class GSSetting<T> {

	private final String displayName;
	private final T defaultValue;
	
	private GSSettingMap settingOwner;
	private boolean active;
	
	public GSSetting(String displayName, T defaultValue) {
		this.displayName = displayName;
		this.defaultValue = defaultValue;
		
		settingOwner = null;
		active = true;
	}
	
	public String getName() {
		return displayName;
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
	
	public abstract void setValue(T value);

	public abstract boolean isSameType(GSSetting<?> other);

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
}
