package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.core.GSCoreOverride;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;

public class GSParentPanel extends GSPanel implements GSParentElement {

	private final List<Element> children;
	private final List<Drawable> drawableWidgets;
	private final List<GSPanel> panels;
	
	private boolean dragging;
	private Element focusedElement;
	
	public GSParentPanel() {
		children = new ArrayList<Element>();
		drawableWidgets = new ArrayList<Drawable>();
		panels = new ArrayList<GSPanel>();
	
		dragging = false;
		focusedElement = null;
	}
	
	@Override
	public void initBounds(MinecraftClient client, int x, int y, int width, int height) {
		// Clear children before GSPanel#init() is called.
		clearChildren();
		
		super.initBounds(client, x, y, width, height);
	}
	
	public void addWidget(Element element) {
		children.add(element);
		
		if (element instanceof Drawable)
			drawableWidgets.add((Drawable)element);
	}
	
	public void addPanel(GSPanel panel) {
		children.add(panel);
		panels.add(panel);

		panel.onAdded();
	}
	
	public void clearChildren() {
		setFocused(null);
		
		children.clear();
		drawableWidgets.clear();

		for (GSPanel panel : panels)
			panel.onRemoved();
		panels.clear();
	}
	
	@Override
	protected void onRemoved() {
		clearChildren();
		
		super.onRemoved();
	}
	
	@Override
	public void tick() {
		super.tick();

		for (GSPanel panel : panels)
			panel.tick();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	protected void renderTranslated(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderTranslated(matrixStack, mouseX, mouseY, partialTicks);

		for (Drawable drawable : drawableWidgets)
			drawable.render(matrixStack, mouseX, mouseY, partialTicks);
		for (GSPanel panel : panels)
			panel.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onMouseMovedGS(double mouseX, double mouseY) {
		GSParentElement.super.onMouseMovedGS(mouseX, mouseY);
	}

	@Override
	public boolean onMouseClickedGS(double mouseX, double mouseY, int button) {
		return GSParentElement.super.onMouseClickedGS(mouseX, mouseY, button);
	}

	@Override
	public boolean onMouseReleasedGS(double mouseX, double mouseY, int button) {
		return GSParentElement.super.onMouseReleasedGS(mouseX, mouseY, button);
	}
	
	public boolean onMouseDraggedGS(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return GSParentElement.super.onMouseDraggedGS(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean onKeyPressedGS(int key, int scancode, int mods) {
		return GSParentElement.super.onKeyPressedGS(key, scancode, mods);
	}

	@Override
	public boolean onKeyReleasedGS(int key, int scancode, int mods) {
		return GSParentElement.super.onKeyReleasedGS(key, scancode, mods);
	}

	@Override
	public boolean onCharTypedGS(char c, int mods) {
		return GSParentElement.super.onCharTypedGS(c, mods);
	}

	@Override
	public boolean onMouseScrolledGS(double mouseX, double mouseY, double scrollX, double scrollY) {
		return GSParentElement.super.onMouseScrolledGS(mouseX, mouseY, scrollX, scrollY);
	}
	
	@Override
	@GSCoreOverride
	public boolean isDragging() {
		return dragging;
	}

	@Override
	@GSCoreOverride
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Override
	@GSCoreOverride
	public Element getFocused() {
		return focusedElement;
	}

	@Override
	@GSCoreOverride
	public void setFocused(Element focused) {
		this.focusedElement = focused;
	}
	
	@Override
	@GSCoreOverride
	public List<Element> children() {
		return this.children;
	}
}