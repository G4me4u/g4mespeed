package com.g4mesoft.setting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GSSettingMap {

	private final GSSettingCategory category;
	private final GSSettingManager owner;
	
	private final Map<Integer, GSSetting<?>> settings;
	
	public GSSettingMap(GSSettingCategory category, GSSettingManager owner) {
		this.category = category;
		this.owner = owner;
		
		settings = new HashMap<Integer, GSSetting<?>>();
	}
	
	public GSSetting<?> getSetting(int key) {
		return settings.get(Integer.valueOf(key));
	}
	
	public void addSetting(GSSetting<?> setting) {
		int settingKey = setting.getIdentifier();
		GSSetting<?> currentSetting = settings.get(settingKey);
		if (currentSetting != null)
			setting.setValueIfSameType(currentSetting);
		
		settings.put(settingKey, setting);
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
