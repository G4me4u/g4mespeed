package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;

public abstract class GSParentGUI extends AbstractParentElement {

	private static final String TRIMMED_TEXT_ELLIPSIS = "...";
	private static final char FORMATTING_CHAR = '\u00A7';
	
	private static final float COLOR_DARKEN_FACTOR = 0.7f;
	
	private boolean selected;
	
	protected MinecraftClient client;
	protected TextRenderer font;
	
	public int x;
	public int y;
	public int width;
	public int height;
	
	private List<Element> children;
	private List<Drawable> drawableWidgets;
	private List<GSParentGUI> gsPanels;
	
	protected GSParentGUI() {
		children = new ArrayList<Element>();
		drawableWidgets = new ArrayList<Drawable>();
		gsPanels = new ArrayList<GSParentGUI>();
		
		setSelected(true);
	}
	
	public void addWidget(Element element) {
		children.add(element);
		
		if (element instanceof Drawable)
			drawableWidgets.add((Drawable)element);
	}
	
	public void addPanel(GSParentGUI panel) {
		gsPanels.add(panel);

		addWidget(panel);
	}
	
	public void clearChildren() {
		children.clear();
		drawableWidgets.clear();
		gsPanels.clear();
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
		for (GSParentGUI panel : gsPanels)
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
		
		GlStateManager.translatef( tx,  ty, 0.0f);
		renderTranslated(mouseX - tx, mouseY - ty, partialTicks);
		GlStateManager.translatef(-tx, -ty, 0.0f);
	}
	
	protected void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		for (Drawable drawable : drawableWidgets)
			drawable.render(mouseX, mouseY, partialTicks);
		for (GSParentGUI panel : gsPanels)
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

	protected String trimText(String text, int availableWidth) {
		return trimText(font, text, availableWidth);
	}
	
	public static String trimText(TextRenderer font, String text, int availableWidth) {
		int len = text.length();
		if (len <= 0)
			return text;

		// Text fits inside bounds.
		if (font.getStringWidth(text) <= availableWidth)
			return text;

		availableWidth -= font.getStringWidth(TRIMMED_TEXT_ELLIPSIS);

		// No space for any other
		// characters.
		if (availableWidth < 0)
			return TRIMMED_TEXT_ELLIPSIS;

		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			// Should probably use getStringWidth
			// and substring instead, but for
			// optimization we use getCharWidth.
			availableWidth -= font.getCharWidth(c);

			if (availableWidth < 0)
				return text.substring(0, i) + TRIMMED_TEXT_ELLIPSIS;
		}

		// This should never happen.
		return text;
	}

	public List<String> splitToLines(String text, int availableWidth) {
		return splitToLines(font, text, availableWidth);
	}
	
	public static List<String> splitToLines(TextRenderer font, String text, int availableWidth) {
		List<String> result = new ArrayList<String>();
		
		int len = text.length();
		if (len <= 0)
			return result;
		
		int lineWidth = 0;
		int lineBegin = 0;
		
		int lastSpaceIndex = -1;
		
		String formattingNextLine = "";
		String formattingThisLine = formattingNextLine;
		
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (c == FORMATTING_CHAR) {
				i++;
				
				if (i < len) {
					c = text.charAt(i);
					if (c == 'r') {
						formattingNextLine = "";
					} else {
						formattingNextLine += Character.toString(FORMATTING_CHAR) + c;
					}
				}
			} else {
				lineWidth += font.getCharWidth(c);
				
				if (c == ' ')
					lastSpaceIndex = i;
				
				if (lineWidth > availableWidth) {
					if (lastSpaceIndex != -1) {
						result.add(formattingThisLine + text.substring(lineBegin, lastSpaceIndex));
						formattingThisLine = formattingNextLine;
						
						i = lastSpaceIndex;
						lineBegin = lastSpaceIndex + 1;
						
						lastSpaceIndex = -1;
					} else {
						result.add(text.substring(lineBegin, i));
						lineBegin = i;
					}
					
					lineWidth = 0;
				}
			}
		}

		if (lineBegin != len)
			result.add(formattingThisLine + text.substring(lineBegin));
		
		return result;
	}
	
	public static int darkenColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
	
		r = (int)(r * COLOR_DARKEN_FACTOR);
		g = (int)(g * COLOR_DARKEN_FACTOR);
		b = (int)(b * COLOR_DARKEN_FACTOR);

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public static int brightenColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
	
		int i = (int)(1.0f / (1.0f - COLOR_DARKEN_FACTOR));
		if (r == 0 && g == 0 && b == 0) {
			r = g = b = i;
		} else {
			if (r > 0 && r < i) r = i;
			if (g > 0 && g < i) g = i;
			if (b > 0 && b < i) b = i;
			
			r = Math.min((int)(r / COLOR_DARKEN_FACTOR), 0xFF);
			g = Math.min((int)(g / COLOR_DARKEN_FACTOR), 0xFF);
			b = Math.min((int)(b / COLOR_DARKEN_FACTOR), 0xFF);
		}

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public GSTranslationModule getTranslationModule() {
		return GSControllerClient.getInstance().getTranslationModule();
	}
	
	@Override
	public List<Element> children() {
		return this.children;
	}
}
