package com.g4mesoft.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;

@Environment(EnvType.CLIENT)
public class GSMainGUI extends Screen {

	protected GSMainGUI() {
		super(NarratorManager.EMPTY);
	}
	
	@Override
	public void render(int width, int height, float partialTicks) {
		super.render(width, height, partialTicks);
		renderBackground();
	}
	
	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
