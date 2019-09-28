package com.g4mesoft.setting;

public interface GSISettingChangeListener {

	public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting);

	public void onSettingAdded(GSSettingCategory category, GSSetting<?> setting);
	
}
