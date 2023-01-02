package com.g4mesoft.setting;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.setting.GSSettingChangePacket.GSESettingChangeType;

public class GSRemoteSettingManager extends GSSettingManager {

	private final GSClientController controllerClient;
	
	private final GSSettingManager shadowSettings;
	private boolean shadowSettingChanging;
	private boolean remoteSettingChanging;

	private boolean allowedSettingChange;
	
	public GSRemoteSettingManager(GSClientController controllerClient) {
		this.controllerClient = controllerClient;
	
		shadowSettings = new GSSettingManager();
		shadowSettingChanging = false;
		remoteSettingChanging = false;
	
		allowedSettingChange = false;
		
		shadowSettings.addChangeListener(new GSISettingChangeListener() {
			@Override
			public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
				if (!shadowSettingChanging) {
					GSSetting<?> currentSetting = getSetting(category, setting.getName());
					if (currentSetting != null)
						currentSetting.setIfSameType(setting);
				}
			}
		});
	}

	@Override
	public void registerSetting(GSSettingCategory category, GSSetting<?> setting) {
		shadowSettings.registerSetting(category, setting);
	}
	
	@Override
	public void removeSetting(GSSettingCategory category, String name) {
		shadowSettings.removeSetting(category, name);
	}
	
	public GSSetting<?> getShadowSetting(GSSettingCategory category, String name) {
		return shadowSettings.getSetting(category, name);
	}
	
	private void updateShadowValue(GSSettingCategory category, GSSetting<?> setting) {
		GSSetting<?> shadowSetting = getShadowSetting(category, setting.getName());
		if (shadowSetting != null) {
			shadowSettingChanging = true;
			shadowSetting.setIfSameType(setting);
			// Make sure client knows whether a remote setting is enabled in GUI.
			shadowSetting.setEnabledInGui(setting.isEnabledInGui());
			shadowSetting.setAllowedChange(setting.isAllowedChange());
			shadowSettingChanging = false;
		}
	}
	
	public void resetRemoteSettings() {
		for (GSSettingMap settingMap : settings.values())
			settingMap.resetSettings();
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
		for (GSSetting<?> setting : settingMap.getSettings())
			onRemoteSettingAdded(category, setting);
	}
	
	public void onRemoteSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		GSSetting<?> currentSetting = getSetting(category, setting.getName());
		
		if (currentSetting != null) {
			remoteSettingChanging = true;
			currentSetting.setIfSameType(setting);
			currentSetting.setEnabledInGui(setting.isEnabledInGui());
			remoteSettingChanging = false;
		}
	}

	public void onRemoteSettingAdded(GSSettingCategory category, GSSetting<?> setting) {
		GSSetting<?> copiedSetting = setting.copySetting();
		copiedSetting.setEnabledInGui(setting.isEnabledInGui());
		copiedSetting.setAllowedChange(allowedSettingChange);
		super.registerSetting(category, copiedSetting);
	}
	
	public void onRemoteSettingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		super.removeSetting(category, setting.getName());
	}

	public void setAllowedSettingChange(boolean allowedSettingChange) {
		if (allowedSettingChange != this.allowedSettingChange) {
			this.allowedSettingChange = allowedSettingChange;
	
			remoteSettingChanging = true;
			for (GSSettingMap settingMap : settings.values()) {
				for (GSSetting<?> setting : settingMap.getSettings())
					setting.setAllowedChange(allowedSettingChange);
			}
			remoteSettingChanging = false;
		}
	}
	
	public boolean isAllowedSettingChange() {
		return allowedSettingChange;
	}
}
