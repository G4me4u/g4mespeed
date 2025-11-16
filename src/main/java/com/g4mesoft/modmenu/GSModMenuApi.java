package com.g4mesoft.modmenu;

import com.g4mesoft.ui.panel.GSPanelContext;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class GSModMenuApi implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			// This is a hack to get the MC screen to be compatible with GSPanel.
			// However, It can generally be guaranteed that setContent is not
			// called after a call to this method, and before the screen closes.
			GSPanelContext.setContent(new GSModMenuConfigPanel(screen));
			return GSPanelContext.getScreen();
		};
	}
}
