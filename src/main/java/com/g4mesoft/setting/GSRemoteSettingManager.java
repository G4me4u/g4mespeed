package com.g4mesoft.setting;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.setting.GSSettingChangePacket.GSESettingChangeType;

public class GSRemoteSettingManager extends GSSettingManager {

	private final GSControllerClient controllerClient;
	
	private boolean remoteSettingChanging;

	public GSRemoteSettingManager(GSControllerClient controllerClient) {
		this.controllerClient = controllerClient;
	
		remoteSettingChanging = false;
	}

	@Override
	public void registerSetting(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingMap settingMap = settings.get(category);
		if (settingMap != null) {
			GSSetting<?> currentSetting = settingMap.getSetting(setting.getName());
			if (currentSetting == null || !currentSetting.isActive())
				setting.setActive(false);
		}
		
		super.registerSetting(category, setting);

		setting.reset();
	}
	
	public void onRemoteSettingMapReceived(GSSettingMap settingMap) {
		if (settingMap.getSettingOwner() == null) {
			GSSettingCategory category = settingMap.getCategory();

			GSSettingMap ownedSettingMap = settings.get(category);
			if (ownedSettingMap == null) {
				ownedSettingMap = new GSSettingMap(category);
				settings.put(category, ownedSettingMap);
			}

			for (GSSetting<?> setting : settingMap.getSettings())
				setting.setActive(true);
			
			ownedSettingMap.transferSettings(settingMap);
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
		super.registerSetting(category, setting);
	}
	
	public void onRemoteSettingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		removeSetting(category, setting.getName());
	}

	@Override
	void settingChanged(GSSettingCategory category, GSSetting<?> setting) {
		super.settingChanged(category, setting);
		
		if (!remoteSettingChanging && setting.isActive())
			controllerClient.sendPacket(new GSSettingChangePacket(category, setting, GSESettingChangeType.SETTING_CHANGED));
	}
}
