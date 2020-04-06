package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.core.GSCoreOverride;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;

public abstract class GSPanel extends AbstractParentElement implements GSIDrawableHelper {

	private boolean selected;
	
	protected MinecraftClient client;
	protected TextRenderer font;
	
	public int x;
	public int y;
	public int width;
	public int height;
	
	private List<Element> children;
	private List<Drawable> drawableWidgets;
	private List<GSPanel> panels;
	
	protected GSPanel() {
		children = new ArrayList<Element>();
		drawableWidgets = new ArrayList<Drawable>();
		panels = new ArrayList<GSPanel>();
		
		setSelected(true);
	}
	
	public void addWidget(Element element) {
		children.add(element);
		
		if (element instanceof Drawable)
			drawableWidgets.add((Drawable)element);
	}
	
	public void addPanel(GSPanel panel) {
		panels.add(panel);

		addWidget(panel);
	}
	
	public void clearChildren() {
		children.clear();
		drawableWidgets.clear();
		panels.clear();
	}
	
	public final void init(MinecraftClient client, int width, int height) {
		initBounds(client, 0, 0, width, height);
	}
	
	public void setBounds(int x, int y, int width, int height) {
		initBounds(client, x, y, width, height);
	}

	public void initBounds(MinecraftClient client, int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	
		this.client = client;
		this.font = client.textRenderer;
		
		clearChildren();
		
		init();
	}

	public void init() {
	}

	public void tick() {
		for (GSPanel panel : panels)
			panel.tick();
	}
	
	protected int getTranslationX() {
		return x;
	}

	protected int getTranslationY() {
		return y;
	}
	
	public void render(int mouseX, int mouseY, float partialTicks) {
		int tx = getTranslationX();
		int ty = getTranslationY();

		BufferBuilder buffer = Tessellator.getInstance().getBufferBuilder();
		GSIBufferBuilderAccess bufferAccess = (GSIBufferBuilderAccess)buffer;
		double oldOffsetX = bufferAccess.getOffsetX();
		double oldOffsetY = bufferAccess.getOffsetY();
		double oldOffsetZ = bufferAccess.getOffsetZ();
		
		buffer.setOffset(oldOffsetX + tx, oldOffsetY + ty, oldOffsetZ);
		renderTranslated(mouseX - tx, mouseY - ty, partialTicks);
		buffer.setOffset(oldOffsetX, oldOffsetY, oldOffsetZ);
	}
	
	protected void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		for (Drawable drawable : drawableWidgets)
			drawable.render(mouseX, mouseY, partialTicks);
		for (GSPanel panel : panels)
			panel.render(mouseX, mouseY, partialTicks);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}
	
	private double translateMouseX(double mouseX) {
		return mouseX - getTranslationX();
	}

	private double translateMouseY(double mouseY) {
		return mouseY - getTranslationY();
	}
	
	@GSCoreOverride
	@Override
	public final void mouseMoved(double mouseX, double mouseY) {
		if (selected)
			mouseMovedTranslated(translateMouseX(mouseX), translateMouseY(mouseY));
	}

	protected void mouseMovedTranslated(double mouseX, double mouseY) {
		hoveredElement(mouseX, mouseY).filter((element) -> {
			element.mouseMoved(mouseX, mouseY);
			return true;
		});
	}

	@GSCoreOverride
	@Override
	public final boolean mouseClicked(double mouseX, double mouseY, int button) {
		return selected && mouseClickedTranslated(translateMouseX(mouseX), translateMouseY(mouseY), button);
	}

	protected boolean mouseClickedTranslated(double mouseX, double mouseY, int button) {
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@GSCoreOverride
	@Override
	public final boolean mouseReleased(double mouseX, double mouseY, int button) {
		return selected && mouseReleasedTranslated(translateMouseX(mouseX), translateMouseY(mouseY), button);
	}

	protected boolean mouseReleasedTranslated(double mouseX, double mouseY, int button) {
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@GSCoreOverride
	@Override
	public final boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return selected && mouseDraggedTranslated(translateMouseX(mouseX), translateMouseY(mouseY), button, dragX, dragY);
	}
	
	protected boolean mouseDraggedTranslated(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@GSCoreOverride
	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		return selected && super.keyPressed(key, scancode, mods);
	}

	@GSCoreOverride
	@Override
	public boolean keyReleased(int key, int scancode, int mods) {
		return selected && super.keyReleased(key, scancode, mods);
	}

	@GSCoreOverride
	@Override
	public boolean charTyped(char c, int mods) {
		return selected && super.charTyped(c, mods);
	}

	@GSCoreOverride
	@Override
	public final boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if (!selected)
			return false;
		return mouseScrolledTranslated(translateMouseX(mouseX), translateMouseY(mouseY), scroll);
	}
	
	protected boolean mouseScrolledTranslated(double mouseX, double mouseY, double scroll) {
		return super.mouseScrolled(mouseX, mouseY, scroll);
	}
	
	@GSCoreOverride
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		if (!selected)
			return false;
		
		return mouseX >= x && mouseX < x + width && 
		       mouseY >= y && mouseY < y + height;
	}

	@Override
	public List<Element> children() {
		return this.children;
	}
	
	@Override
	public TextRenderer getFont() {
		return font;
	}
}
