package com.g4mesoft.setting;

public abstract class GSSetting<T> {

	private final String displayName;
	private final int identifier;
	private final T defaultValue;
	
	private GSSettingMap settingOwner;
	
	public GSSetting(String displayName, int identifier, T defaultValue) {
		this.displayName = displayName;
		this.identifier = identifier;
		this.defaultValue = defaultValue;
		
		settingOwner = null;
	}
	
	public String getName() {
		return displayName;
	}
	
	public int getIdentifier() {
		return identifier;
	}
	
	void setSettingOwner(GSSettingMap changeListener) {
		if (this.settingOwner != null)
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
}
