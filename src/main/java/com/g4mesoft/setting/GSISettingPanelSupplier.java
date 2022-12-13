package com.g4mesoft.setting;

import com.g4mesoft.gui.setting.GSSettingPanel;

public interface GSISettingPanelSupplier<T extends GSSetting<?>> {

	public GSSettingPanel<T> create(GSSettingCategory category, T setting);
	
}
