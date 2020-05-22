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
import net.minecraft.util.Identifier;

public class GSScrollBar extends DrawableHelper implements Drawable {

	private static final Identifier TEXTURE = new Identifier("g4mespeed/textures/scroll_bar.png");
	
	private static final int KNOB_AREA_COLOR = 0xFF7F7F7F;
	private static final int DISABLED_KNOB_AREA_COLOR = 0xFF595959;

	private static final int KNOB_COLOR = 0xFFC6C6C6;
	private static final int HOVERED_KNOB_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_KNOB_COLOR = 0xFF7F7F7F;
	
	private static final double SCROLL_AMOUNT = 20.0;
	
	public static final int SCROLL_BAR_WIDTH = 9;
	private static final int BUTTON_WIDTH = 9;
	private static final int BUTTON_HEIGHT = 10;
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
	
	public GSScrollBar(boolean vertical, GSIScrollableViewport parent, GSIScrollListener listener) {
		this.vertical = vertical;
		this.parent = parent;
		this.listener = listener;
	
		enabled = true;
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
		
		// Update the scroll offset to ensure that it is valid.
		setScrollOffset(scrollOffset);
	}

	@Override
	@GSCoreOverride
	public void render(int mouseX, int mouseY, float partialTicks) {
		drawScrollButton(mouseX, mouseY, x, y, true);
		drawScrollButton(mouseX, mouseY, x + width - getButtonSizeX(), y + height - getButtonSizeY(), false);
		
		drawKnobArea();
		drawKnob(mouseX, mouseY);
	}
	
	protected Identifier getScrollButtonTexture() {
		return TEXTURE;
	}
	
	protected int getKnobAreaColor() {
		if (!enabled)
			return DISABLED_KNOB_AREA_COLOR;
		return KNOB_AREA_COLOR;
	}
	
	protected int getKnobColor(boolean hovered) {
		if (!enabled)
			return DISABLED_KNOB_COLOR;
		if (scrollDragActive || hovered)
			return HOVERED_KNOB_COLOR;
		return KNOB_COLOR;
	}
	
	private void drawScrollButton(int mouseX, int mouseY, int bx, int by, boolean top) {
		client.getTextureManager().bindTexture(getScrollButtonTexture());
		
		GlStateManager.disableBlend();
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		int bsx = getButtonSizeX();
		int bsy = getButtonSizeY();
		
		int sy = vertical ? (top ? 0 : 1) : (top ? 2 : 3);
		int sx;
		if (enabled) {
			sx = (mouseX >= bx && mouseX < bx + bsx && 
					mouseY >= by && mouseY < by + bsy) ? 1 : 0;
		} else {
			sx = 2;
		}
		
		blit(bx, by, sx * 10, sy * 10, bsx, bsy, 30, 40);
	}
	
	private int getButtonSizeX() {
		return vertical ? BUTTON_WIDTH : BUTTON_HEIGHT;
	}

	private int getButtonSizeY() {
		return vertical ? BUTTON_HEIGHT : BUTTON_WIDTH;
	}

	private void drawKnobArea() {
		int x0 = vertical ? x : (x + BUTTON_HEIGHT);
		int y0 = vertical ? (y + BUTTON_HEIGHT) : y;
		int x1 = vertical ? (x + width) : (x + width - BUTTON_HEIGHT);
		int y1 = vertical ? (y + height - BUTTON_HEIGHT) : (y + height);
		
		fill(x0, y0, x1, y1, 0xFF000000);
		
		if (vertical) {
			x0++;
			x1--;
		} else {
			y0++;
			y1--;
		}

		fill(x0, y0, x1, y1, getKnobAreaColor());
	}
	
	private void drawKnob(int mouseX, int mouseY) {
		int knobSize = getKnobSize();
		int knobPos = getKnobPos(knobSize);
		
		int kx = vertical ? (x + 1) : knobPos;
		int ky = vertical ? knobPos : (y + 1);
		int kw = vertical ? (width - 2) : knobSize;
		int kh = vertical ? knobSize : (height - 2);
		
		fill(kx, ky, kx + kw, ky + kh, getKnobColor(isMouseOverKnob(mouseX, mouseY)));
	}
	
	private boolean isMouseOverKnob(int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY))
			return false;
		
		int ks = getKnobSize();
		int kp = getKnobPos(ks);
		
		int mp = vertical ? mouseY : mouseX;
		return (mp >= kp && mp < kp + ks);
	}
	
	private int getKnobSize() {
		return Math.max(getKnobAreaSize() * getViewSize() / getContentSize(), MINIMUM_NOB_SIZE);
	}

	private int getKnobPos(int knobSize) {
		int maxScroll = getMaxScrollOffset();
		int pos = (vertical ? y : x) + BUTTON_HEIGHT;
		
		if (maxScroll > 0)
			pos += (int)((getKnobAreaSize() - knobSize) * scrollOffset / maxScroll);
		return pos;
	}
	
	private int getKnobAreaSize() {
		return (vertical ? height : width) - BUTTON_HEIGHT * 2;
	}
	
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		// In case the user is trying to zoom in or out we should not
		// scroll. This has different behavior on different platforms.
		if (Screen.hasControlDown())
			return false;
		
		// Shift will flip the xScroll and yScroll. This makes it
		// possible to scroll horizontally without any xScroll.
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
		
		if (button == GLFW.GLFW_MOUSE_BUTTON_1)
			scrollDragActive = false;
		
		return false;
	}
	
	private boolean isMouseOver(double mouseX, double mouseY) {
		return (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
	}
	
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1)
			scrollDragActive = false;
		return false;
	}
	
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_1 && scrollDragActive) {
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
	
	public boolean isVertical() {
		return vertical;
	}
	
	public boolean isScrollDragActive() {
		return scrollDragActive;
	}
}
