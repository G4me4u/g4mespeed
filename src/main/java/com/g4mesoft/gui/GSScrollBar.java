package com.g4mesoft.gui;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.access.GSIMouseAccess;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.util.GSMathUtils;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class GSScrollBar extends DrawableHelper implements Drawable {

	private static final Identifier TEXTURE_LIGHT = new Identifier("g4mespeed/textures/scroll_bar_light.png");
	private static final Identifier TEXTURE_DARK = new Identifier("g4mespeed/textures/scroll_bar_dark.png");
	
	private static final int LIGHT_KNOB_AREA_COLOR = 0xFFC6C6C6;
	private static final int LIGHT_KNOB_COLOR = 0xFFFFFFFF;
	private static final int LIGHT_DISABLED_KNOB_AREA_COLOR = 0xFF595959;
	private static final int LIGHT_DISABLED_KNOB_COLOR = 0xFF7F7F7F;

	private static final int DARK_KNOB_AREA_COLOR = 0xFF2B2A2B;
	private static final int DARK_KNOB_COLOR = 0xFF595959;
	private static final int DARK_DISABLED_KNOB_AREA_COLOR = 0xFF000000;
	private static final int DARK_DISABLED_KNOB_COLOR = 0xFF2B2A2B;
	
	private static final double SCROLL_AMOUNT = 20.0;
	
	public static final int SCROLL_BAR_WIDTH = 8;
	private static final int BUTTON_SIZE = SCROLL_BAR_WIDTH;
	private static final int MINIMUM_NOB_SIZE = 10;
	
	private final boolean vertical;
	private final GSIScrollableViewport parent;
	private final GSIScrollListener listener;

	private MinecraftClient client;
	
	private int x;
	private int y;
	private int width;
	private int height;
	
	private boolean scrollDragActive;
	private double scrollOffset;
	
	private boolean enabled;
	private boolean darkMode;
	
	public GSScrollBar(boolean vertical, GSIScrollableViewport parent, GSIScrollListener listener) {
		this.vertical = vertical;
		this.parent = parent;
		this.listener = listener;
	
		enabled = true;
		darkMode = false;
	}

	public void init(MinecraftClient client, int marginX, int marginY) {
		init(client, marginX, marginX, marginY, marginY);
	}
	
	public void init(MinecraftClient client, int ml, int mr, int mt, int mb) {
		this.client = client;
		
		if (vertical) {
			x = parent.getWidth() - mr - SCROLL_BAR_WIDTH;
			y = mt;
			width = SCROLL_BAR_WIDTH;
			height = parent.getHeight() - mt - mb;
		} else {
			x = ml;
			y = parent.getHeight() - mb - SCROLL_BAR_WIDTH;
			width = parent.getWidth() - ml - mr;
			height = SCROLL_BAR_WIDTH;
		}
		
		// Update the scroll offset to ensure
		// that it is valid.
		setScrollOffset(scrollOffset);
	}

	@Override
	@GSCoreOverride
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		drawScrollButton(matrixStack, mouseX, mouseY, x, y, true);
		drawScrollButton(matrixStack, mouseX, mouseY, x + width - BUTTON_SIZE, y + height - BUTTON_SIZE, false);
		
		drawKnobArea(matrixStack);
		drawKnob(matrixStack);
	}
	
	private void drawScrollButton(MatrixStack matrixStack, int mouseX, int mouseY, int bx, int by, boolean top) {
		client.getTextureManager().bindTexture(darkMode ? TEXTURE_DARK : TEXTURE_LIGHT);
		
		GlStateManager.disableBlend();
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		int bsy = vertical ? (top ? 0 : 8) : (top ? 16 : 24);
		int bsx;
		if (enabled) {
			bsx = (mouseX >= bx && mouseX < bx + BUTTON_SIZE && 
					mouseY >= by && mouseY < by + BUTTON_SIZE) ? 8 : 0;
		} else {
			bsx = 16;
		}
		
		drawTexture(matrixStack, bx, by, bsx, bsy, BUTTON_SIZE, BUTTON_SIZE, 24, 32);
	}
	
	private void drawKnobArea(MatrixStack matrixStack) {
		if (vertical) {
			fill(matrixStack, x, y + BUTTON_SIZE, x + width, y + height - BUTTON_SIZE, getKnobAreaColor());
		} else {
			fill(matrixStack, x + BUTTON_SIZE, y, x + width - BUTTON_SIZE, y + height, getKnobAreaColor());
		}
	}
	
	private void drawKnob(MatrixStack matrixStack) {
		int knobSize = getKnobSize();
		int knobPos = getKnobPos(knobSize);
		
		if (vertical) {
			fill(matrixStack, x + 1, knobPos, x + width - 1, knobPos + knobSize, getKnobColor());
		} else {
			fill(matrixStack, knobPos, y + 1, knobPos + knobSize, y + height - 1, getKnobColor());
		}
	}
	
	private int getKnobAreaColor() {
		if (darkMode)
			return enabled ? DARK_KNOB_AREA_COLOR : DARK_DISABLED_KNOB_AREA_COLOR;
		return enabled ? LIGHT_KNOB_AREA_COLOR : LIGHT_DISABLED_KNOB_AREA_COLOR;
	}

	private int getKnobColor() {
		if (darkMode)
			return enabled ? DARK_KNOB_COLOR : DARK_DISABLED_KNOB_COLOR;
		return enabled ? LIGHT_KNOB_COLOR : LIGHT_DISABLED_KNOB_COLOR;
	}
	
	private int getKnobSize() {
		return Math.max(getKnobAreaSize() * getViewSize() / getContentSize(), MINIMUM_NOB_SIZE);
	}

	private int getKnobPos(int knobSize) {
		int maxScroll = getMaxScrollOffset();
		int pos = (vertical ? y : x) + BUTTON_SIZE;
		
		if (maxScroll > 0)
			pos += (int)((getKnobAreaSize() - knobSize) * scrollOffset / maxScroll);
		return pos;
	}
	
	private int getKnobAreaSize() {
		return (vertical ? height : width) - BUTTON_SIZE * 2;
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		// In case the user is trying to zoom in
		// or out we shouldn't scroll.
		if (Screen.hasControlDown())
			return false;
		
		double actualScroll = (vertical != Screen.hasShiftDown()) ? scroll : ((GSIMouseAccess)client.mouse).getScrollX(); 
		if (enabled && mouseX >= 0.0 && mouseY >= 0.0 && mouseX < parent.getWidth() && mouseY < parent.getHeight()) {
			setScrollOffset(scrollOffset - actualScroll * SCROLL_AMOUNT);
			return true;
		}
		
		return false;
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isMouseOver(mouseX, mouseY)) {
			if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_1) {
				double mousePos = vertical ? mouseY : mouseX;

				int knobSize = getKnobSize();
				int knobPos = getKnobPos(knobSize);
				if (mousePos < knobPos) {
					setScrollOffset(scrollOffset - SCROLL_AMOUNT);
				} else if (mousePos >= knobPos + knobSize) {
					setScrollOffset(scrollOffset + SCROLL_AMOUNT);
				} else {
					scrollDragActive = true;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean isMouseOver(double mouseX, double mouseY) {
		return (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
	}
	
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		scrollDragActive = false;
		return false;
	}
	
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (enabled && scrollDragActive) {
			double delta = vertical ? dragY : dragX;

			int compAreaSize = (getKnobAreaSize() - getKnobSize());
			if (compAreaSize != 0)
				delta *= (double)(getContentSize() - getViewSize()) / compAreaSize;
			
			setScrollOffset(scrollOffset + delta);
			return true;
		}
		
		return false;
	}
	
	public void setScrollOffset(double scroll) {
		if (listener != null)
			listener.preScrollChanged(scroll);
		
		scrollOffset = GSMathUtils.clamp(scroll, 0.0, getMaxScrollOffset());
		
		if (listener != null)
			listener.scrollChanged(scrollOffset);
	}
	
	private int getMaxScrollOffset() {
		return Math.max(getContentSize() - getViewSize(), 0);
	}
	
	public double getScrollOffset() {
		return scrollOffset;
	}
	
	private int getViewSize() {
		return vertical ? parent.getHeight() : parent.getWidth();
	}

	private int getContentSize() {
		return vertical ? parent.getContentHeight() : parent.getContentWidth();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isDarkMode() {
		return darkMode;
	}
	
	public void setDarkMode(boolean darkMode) {
		this.darkMode = darkMode;
	}
	
	public boolean isVertical() {
		return vertical;
	}
}
