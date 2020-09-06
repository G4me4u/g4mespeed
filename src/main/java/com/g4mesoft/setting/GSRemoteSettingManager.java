package com.g4mesoft.setting;

import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.setting.GSSettingChangePacket.GSESettingChangeType;

public class GSRemoteSettingManager extends GSSettingManager {

	private final GSControllerClient controllerClient;
	
	private final Map<GSSettingCategory, GSSettingMap> shadowSettings;
	private boolean remoteSettingChanging;

	private boolean allowedSettingChange;
	
	public GSRemoteSettingManager(GSControllerClient controllerClient) {
		this.controllerClient = controllerClient;
	
		shadowSettings = new HashMap<>();
		remoteSettingChanging = false;
	
		allowedSettingChange = false;
	}

	@Override
	public void registerSetting(GSSettingCategory category, GSSetting<?> setting) {
		registerShadowSetting(category, setting);
	}

	public void registerShadowSetting(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingMap shadowSettingMap = shadowSettings.get(category);
		if (shadowSettingMap == null) {
			// We don't want to receive events from the
			// shadow settings. Hence we set owner to null
			shadowSettingMap = new GSSettingMap(category, null);
			shadowSettings.put(category, shadowSettingMap);
		}
		
		shadowSettingMap.registerSetting(setting);
	}
	
	public GSSetting<?> getShadowSetting(GSSettingCategory category, String name) {
		GSSettingMap categorySettings = shadowSettings.get(category);
		return (categorySettings != null) ? categorySettings.getSetting(name) : null;
	}
	
	private void updateShadowValue(GSSettingCategory category, GSSetting<?> setting) {
		GSSetting<?> shadowSetting = getShadowSetting(category, setting.getName());
		if (shadowSetting != null)
			shadowSetting.setValueIfSameType(setting);
	}
	
	@Override
	void settingChanged(GSSettingCategory category, GSSetting<?> setting) {
		super.settingChanged(category, setting);
		
		updateShadowValue(category, setting);
		
		if (!remoteSettingChanging && allowedSettingChange)
			controllerClient.sendPacket(new GSSettingChangePacket(category, setting, GSESettingChangeType.SETTING_CHANGED));
	}

	@Override
	void settingAdded(GSSettingCategory category, GSSetting<?> setting) {
		super.settingAdded(category, setting);
		
		updateShadowValue(category, setting);
	}
	
	@Override
	void settingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		super.settingRemoved(category, setting);
		
		GSSetting<?> shadowSetting = getShadowSetting(category, setting.getName());
		if (shadowSetting != null)
			shadowSetting.reset();
	}
	
	public void onRemoteSettingMapReceived(GSSettingMap settingMap) {
		GSSettingCategory category = settingMap.getCategory();

		for (GSSetting<?> setting : settingMap.getSettings()) {
			super.registerSetting(category, setting.copySetting().setEnabledInGui(allowedSettingChange));
		}
	}
	
	public void onRemoteSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		GSSetting<?> currentSetting = getSetting(category, setting.getName());
		
		if (currentSetting != null) {
			remoteSettingChanging = true;
			currentSetting.setValueIfSameType(setting);
			remoteSettingChanging = false;
		}
	}

	public void onRemoteSettingAdded(GSSettingCategory category, GSSetting<?> setting) {
		super.registerSetting(category, setting.copySetting().setEnabledInGui(allowedSettingChange));
	}
	
	public void onRemoteSettingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		removeSetting(category, setting.getName());
	}

	public void setAllowedSettingChange(boolean allowedSettingChange) {
		if (allowedSettingChange != this.allowedSettingChange) {
			this.allowedSettingChange = allowedSettingChange;
	
			for (GSSettingMap settingMap : settings.values()) {
				for (GSSetting<?> setting : settingMap.getSettings())
					setting.setEnabledInGui(allowedSettingChange);
			}
		}
	}
	
	public boolean isAllowedSettingChange() {
		return allowedSettingChange;
	}
}
