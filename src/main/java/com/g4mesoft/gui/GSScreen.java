package com.g4mesoft.gui;

import com.g4mesoft.core.GSCoreOverride;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;

public abstract class GSScreen extends Screen implements GSIDrawableHelper {

	protected GSScreen() {
		super(NarratorManager.EMPTY);
	}

	@Override
	@GSCoreOverride
	public void mouseMoved(double mouseX, double mouseY) {
		hoveredElement(mouseX, mouseY).filter((element) -> {
			element.mouseMoved(mouseX, mouseY);
			return true;
		});
	}
	
	@Override
	public TextRenderer getFont() {
		return font;
	}
}
