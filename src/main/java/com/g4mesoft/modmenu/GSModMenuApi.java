package com.g4mesoft.modmenu;

import java.util.function.Function;

import com.g4mesoft.ui.panel.GSPanelContext;

import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class GSModMenuApi implements ModMenuApi {

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return screen -> {
			// This is a hack to get the MC screen to be compatible with GSPanel.
			// However, It can generally be guaranteed that setContent is not
			// called after a call to this method, and before the screen closes.
			GSPanelContext.setContent(new GSModMenuConfigPanel(screen));
			return GSPanelContext.getScreen();
		};
	}

	@Override
	public String getModId() {
		return "g4mespeed";
	}
}
