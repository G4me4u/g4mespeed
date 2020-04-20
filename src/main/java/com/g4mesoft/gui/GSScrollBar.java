package com.g4mesoft.gui;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.util.GSMathUtils;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

public class GSScrollBar extends DrawableHelper implements Drawable {

	private static final Identifier TEXTURE = new Identifier("g4mespeed/textures/scroll_bar.png");
	
	private static final int KNOB_AREA_COLOR = 0xFFC6C6C6;
	private static final int DISABLED_KNOB_AREA_COLOR = 0xFF595959;
	private static final int KNOB_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_KNOB_COLOR = 0xFF7F7F7F;
	
	private static final double SCROLL_AMOUNT = 20.0;
	
	private static final int SCROLL_BAR_WIDTH    = 8;
	private static final int BUTTON_SIZE         = SCROLL_BAR_WIDTH;
	
	private static final int MINIMUM_NOB_SIZE    = 10;
	
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
	
	public GSScrollBar(boolean vertical, GSIScrollableViewport parent, GSIScrollListener listener) {
		this.vertical = vertical;
		this.parent = parent;
		this.listener = listener;
	
		enabled = true;
	}
	
	public void init(MinecraftClient client, int marginX, int marginY) {
		this.client = client;
		
		if (vertical) {
			x = parent.getWidth() - marginX - SCROLL_BAR_WIDTH;
			y = marginY;
			width = SCROLL_BAR_WIDTH;
			height = parent.getHeight() - marginY * 2;
		} else {
			x = marginX;
			y = parent.getHeight() - marginY - SCROLL_BAR_WIDTH;
			width = parent.getWidth() - marginX * 2;
			height = SCROLL_BAR_WIDTH;
		}
		
		// Update the scroll offset to ensure
		// that it is valid.
		setScrollOffset(scrollOffset);
	}

	@Override
	@GSCoreOverride
	public void render(int mouseX, int mouseY, float partialTicks) {
		drawScrollButton(mouseX, mouseY, x, y, true);
		drawScrollButton(mouseX, mouseY, x + width - BUTTON_SIZE, y + height - BUTTON_SIZE, false);
		
		drawKnobArea();
		drawKnob();
	}
	
	private void drawScrollButton(int mouseX, int mouseY, int bx, int by, boolean top) {
		client.getTextureManager().bindTexture(TEXTURE);
		
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
		
		blit(bx, by, bsx, bsy, BUTTON_SIZE, BUTTON_SIZE, 24, 32);
	}
	
	private void drawKnobArea() {
		int color = enabled ? KNOB_AREA_COLOR : DISABLED_KNOB_AREA_COLOR;
		
		if (vertical) {
			fill(x, y + BUTTON_SIZE, x + width, y + height - BUTTON_SIZE, color);
		} else {
			fill(x + BUTTON_SIZE, y, x + width - BUTTON_SIZE, y + height, color);
		}
	}
	
	private void drawKnob() {
		int knobSize = getKnobSize();
		int knobPos = getKnobPos(knobSize);
		
		int color = enabled ? KNOB_COLOR : DISABLED_KNOB_COLOR;
		
		if (vertical) {
			fill(x + 1, knobPos, x + width - 1, knobPos + knobSize, color);
		} else {
			fill(knobPos, y + 1, knobPos + knobSize, y + height - 1, color);
		}
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
		if (vertical == Screen.hasShiftDown())
			return false;
		
		if (enabled && mouseX >= 0.0 && mouseY >= 0.0 && mouseX < parent.getWidth() && mouseY < parent.getHeight()) {
			setScrollOffset(scrollOffset - scroll * SCROLL_AMOUNT);
			return true;
		}
		
		return false;
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_1 && isMouseOver(mouseX, mouseY)) {
			if (isMouseOverNob(mouseX, mouseY)) {
				scrollDragActive = true;
				return true;
			} else if (mouseY < y + BUTTON_SIZE) {
				setScrollOffset(scrollOffset - SCROLL_AMOUNT);
				return true;
			} else if (mouseY > y + height - BUTTON_SIZE) {
				setScrollOffset(scrollOffset + SCROLL_AMOUNT);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isMouseOver(double mouseX, double mouseY) {
		return (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
	}
	
	private boolean isMouseOverNob(double mouseX, double mouseY) {
		double mousePos;
		if (vertical) {
			if (mouseX < 0.0 && mouseX >= width)
				return false;
			mousePos = mouseY;
		} else {
			if (mouseY < 0.0 && mouseY >= height)
				return false;
			mousePos = mouseX;
		}
		
		int knobSize = getKnobSize();
		int knobPos = getKnobPos(knobSize);
		return (mousePos >= knobPos && mousePos < knobPos + knobSize);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		scrollDragActive = false;
		return false;
	}
	
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (enabled && scrollDragActive) {
			double drag = vertical ? dragY : dragX;
			double delta = drag * (getContentSize() - getViewSize()) / (getKnobAreaSize() - getKnobSize());
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
	
	public boolean isVertical() {
		return vertical;
	}
}
