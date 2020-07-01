package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.access.GSIMouseAccess;
import com.g4mesoft.core.GSCoreOverride;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.NarratorManager;

public abstract class GSScreen extends Screen implements GSParentElement, GSIDrawableHelper, GSIViewport {

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
	
	@SuppressWarnings("deprecation")
	protected void renderPanels(int mouseX, int mouseY, float partialTicks) {
		for (GSPanel panel : panels)
			panel.render(mouseX, mouseY, partialTicks);
	}
	
	@Override
	@Deprecated
	@GSCoreOverride
	public final void mouseMoved(double mouseX, double mouseY) {
		onMouseMovedGS(mouseX, mouseY);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseClicked(double mouseX, double mouseY, int button) {
		return onMouseClickedGS(mouseX, mouseY, button);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseReleased(double mouseX, double mouseY, int button) {
		return onMouseReleasedGS(mouseX, mouseY, button);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return onMouseDraggedGS(mouseX, mouseY, button, dragX, dragY);
	}
	
	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean keyPressed(int key, int scancode, int mods) {
		if (onKeyPressedGS(key, scancode, mods))
			return true;
		
		if (key == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		}
		
		if (key == GLFW.GLFW_KEY_TAB) {
			boolean reverse = hasShiftDown();
			if (!this.changeFocus(!reverse))
				this.changeFocus(!reverse);
			return true;
		}

		return false;
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean keyReleased(int key, int scancode, int mods) {
		return onKeyReleasedGS(key, scancode, mods);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean charTyped(char c, int mods) {
		return onCharTypedGS(c, mods);
	}
	
	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		double scrollX = ((GSIMouseAccess)MinecraftClient.getInstance().mouse).getScrollX();
		return onMouseScrolledGS(mouseX, mouseY, scrollX, scrollY);
	}
	
	@Override
	public boolean isAdded() {
		return (MinecraftClient.getInstance().currentScreen == this);
	}
	
	@Override
	public void setFocused(boolean focused) {
	}
	
	@Override
	public boolean isFocused() {
		return isAdded();
	}
	
	@Override
	public boolean isEditingText() {
		return false;
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
