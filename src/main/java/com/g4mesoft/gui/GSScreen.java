package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.core.GSCoreOverride;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;

public abstract class GSScreen extends Screen implements GSIDrawableHelper, GSIViewport {

	private boolean initialized;
	
	private final List<GSPanel> panels;
	
	protected GSScreen() {
		super(NarratorManager.EMPTY);
	
		initialized = false;
		
		panels = new ArrayList<GSPanel>();
	}
	
	public void addPanel(GSPanel panel) {
		panels.add(panel);
		children.add(panel);
		
		panel.onAdded();
	}
	
	public void clearChildren() {
		setFocused(null);
		
		for (GSPanel panel : panels)
			panel.onRemoved();
		
		panels.clear();
		children.clear();
	}
	
	@Override
	@GSCoreOverride
	public void init(MinecraftClient client, int width, int height) {
		clearChildren();
		
		super.init(client, width, height);

		if (!initialized) {
			initialized = true;
			onAdded();
		}
	}
	
	@Override
	@GSCoreOverride
	public void removed() {
		onRemoved();
	}
	
	protected void onAdded() {
	}

	protected void onRemoved() {
		clearChildren();
	}

	@Override
	@GSCoreOverride
	public void tick() {
		super.tick();
		
		tickPanels();
	}
	
	protected void tickPanels() {
		for (GSPanel panel : panels)
			panel.tick();
	}
	
	@Override
	@GSCoreOverride
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		
		renderPanels(mouseX, mouseY, partialTicks);
	}
	
	protected void renderPanels(int mouseX, int mouseY, float partialTicks) {
		for (GSPanel panel : panels)
			panel.render(mouseX, mouseY, partialTicks);
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
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
}
