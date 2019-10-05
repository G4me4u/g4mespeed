package com.g4mesoft.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GSSettingManager {

	private final Map<GSSettingCategory, GSSettingMap> settings;
	private final List<GSISettingChangeListener> listeners;
	
	public GSSettingManager() {
		settings = new HashMap<GSSettingCategory, GSSettingMap>();
		listeners = new ArrayList<GSISettingChangeListener>();
	}

	public GSSetting<?> getSetting(GSSettingCategory category, String name) {
		GSSettingMap categorySettings = settings.get(category);
		if (categorySettings == null)
			return null;
		return categorySettings.getSetting(name);
	}
	
	public void addSetting(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingMap categorySettings = settings.get(category);
		if (categorySettings == null) {
			categorySettings = new GSSettingMap(category, this);
			settings.put(category, categorySettings);
		}
		
		categorySettings.addSetting(setting);
		
		for (GSISettingChangeListener listener : listeners)
			listener.onSettingAdded(category, setting);
	}
	
	public Collection<GSSettingMap> getSettings() {
		return Collections.unmodifiableCollection(settings.values());
	}
	
	void settingChanged(GSSettingCategory category, GSSetting<?> setting) {
		for (GSISettingChangeListener listener : listeners)
			listener.onSettingChanged(category, setting);
	}

	public void addChangeListener(GSISettingChangeListener changeListener) {
		listeners.add(changeListener);
	}

	public void removeChangeListener(GSISettingChangeListener changeListener) {
		listeners.remove(changeListener);
	}
}
