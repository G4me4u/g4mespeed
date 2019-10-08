package com.g4mesoft.setting;

public interface GSISettingChangeListener {

	default public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) { }

	default public void onSettingAdded(GSSettingCategory category, GSSetting<?> setting) { }

	default public void onSettingRemoved(GSSettingCategory category, GSSetting<?> setting) { }
	
}
