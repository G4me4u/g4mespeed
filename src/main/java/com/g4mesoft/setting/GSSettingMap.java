package com.g4mesoft.setting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GSSettingMap {

	private final GSSettingCategory category;
	private final GSSettingManager owner;
	
	private final Map<String, GSSetting<?>> settings;
	
	public GSSettingMap(GSSettingCategory category, GSSettingManager owner) {
		this.category = category;
		this.owner = owner;
		
		settings = new HashMap<String, GSSetting<?>>();
	}
	
	public GSSetting<?> getSetting(String name) {
		return settings.get(name);
	}
	
	public void addSetting(GSSetting<?> setting) {
		GSSetting<?> currentSetting = getSetting(setting.getName());
		if (currentSetting != null)
			setting.setValueIfSameType(currentSetting);
		
		settings.put(setting.getName(), setting);
		setting.setSettingOwner(this);
	}
	
	public Collection<GSSetting<?>> getSettings() {
		return Collections.unmodifiableCollection(settings.values());
	}

	void settingChanged(GSSetting<?> setting) {
		owner.settingChanged(category, setting);
	}

	public GSSettingCategory getCategory() {
		return category;
	}
}
