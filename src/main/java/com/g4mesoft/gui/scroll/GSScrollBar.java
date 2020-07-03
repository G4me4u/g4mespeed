package com.g4mesoft.gui.scroll;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.gui.GSPanel;
import com.g4mesoft.util.GSMathUtils;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

public class GSScrollBar extends GSPanel {

	private static final Identifier SCROLL_BUTTON_TEXTURE = new Identifier("g4mespeed/textures/scroll_bar.png");
	
	private static final int KNOB_AREA_COLOR = 0xFF7F7F7F;
	private static final int DISABLED_KNOB_AREA_COLOR = 0xFF595959;

	private static final int KNOB_COLOR = 0xFFC6C6C6;
	private static final int HOVERED_KNOB_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_KNOB_COLOR = 0xFF7F7F7F;
	
	private static final double DEFAULT_SCROLL_AMOUNT = 20.0;
	
	private static final int DEFAULT_SCROLL_BAR_WIDTH = 9;
	private static final int DEFAULT_BUTTON_WIDTH = 9;
	private static final int DEFAULT_BUTTON_HEIGHT = 10;
	private static final int DEFAULT_MINIMUM_NOB_SIZE = 10;
	
	protected final GSIScrollableViewport parent;
	private final GSIScrollListener listener;
	
	private boolean vertical;

	private boolean scrollDragActive;
	private double scrollOffset;
	
	private boolean enabled;
	
	/**
	 * <b>NOTE:</b> parent must be the GSParentElement that dispatches events to this
	 * scroll bar. If this is not the case, mouse scrolling might have undefined
	 * behavior.
	 * 
	 * @param parent
	 * @param listener
	 */
	public GSScrollBar(GSIScrollableViewport parent, GSIScrollListener listener) {
		this.parent = parent;
		this.listener = listener;

		// Vertical by default
		vertical = true;
		
		enabled = true;
	}

	public void initVerticalLeft(MinecraftClient client, int xl, int yt, int height) {
		initBounds(client, xl, yt, getPreferredScrollBarWidth(), height);
		
		vertical = true;
	}

	public void initVerticalRight(MinecraftClient client, int xr, int yt, int height) {
		initVerticalLeft(client, xr - getPreferredScrollBarWidth(), yt, height);
	}
	
	public void initHorizontalTop(MinecraftClient client, int xl, int yt, int width) {
		initBounds(client, xl, yt, width, getPreferredScrollBarWidth());

		vertical = false;
	}

	public void initHorizontalBottom(MinecraftClient client, int xl, int yb, int width) {
		initHorizontalTop(client, xl, yb - getPreferredScrollBarWidth(), width);
	}
	
	/**
	 * @see GSScrollBar#initVerticalLeft(MinecraftClient, int, int, int)
	 * @see GSScrollBar#initVerticalRight(MinecraftClient, int, int, int)
	 * @see GSScrollBar#initHorizontalTop(MinecraftClient, int, int, int)
	 * @see GSScrollBar#initHorizontalBottom(MinecraftClient, int, int, int)
	 */
	@Override
	@Deprecated
	public final void initBounds(MinecraftClient client, int x, int y, int width, int height) {
		super.initBounds(client, x, y, width, height);
	}
	
	@Override
	public void init() {
		super.init();

		// Update the scroll offset to ensure that it is valid.
		setScrollOffset(scrollOffset);
	}
	
	@Override
	public void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		drawScrollButtons(mouseX, mouseY);
		
		drawKnobArea();
		drawKnob(mouseX, mouseY, isMouseOverKnob(mouseX, mouseY));
	}
	
	protected void drawScrollButtons(int mouseX, int mouseY) {
		client.getTextureManager().bindTexture(getScrollButtonTexture());
		
		GlStateManager.disableBlend();
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		drawScrollButton(mouseX, mouseY, true);
		drawScrollButton(mouseX, mouseY, false);
	}

	protected void drawScrollButton(int mouseX, int mouseY, boolean top) {
		int bp = top ? 0 : (isVertical() ? height : width) - getButtonHeight();
		boolean hovered = isMouseOverScrollButton(mouseX, mouseY, top);

		if (isVertical()) {
			drawScrollButton(0, bp, getButtonWidth(), getButtonHeight(), top, hovered);
		} else {
			drawScrollButton(bp, 0, getButtonHeight(), getButtonWidth(), top, hovered);
		}
	}
	
	protected void drawScrollButton(int bx, int by, int bw, int bh, boolean top, boolean hovered) {
		int sx = getScrollButtonSpriteX(top, hovered);
		int sy = getScrollButtonSpriteY(top, hovered);
		blit(bx, by, sx, sy, bw, bh, getScrollButtonSpriteWidth(), getScrollButtonSpriteHeight());
	}

	protected int getScrollButtonSpriteX(boolean top, boolean hovered) {
		return enabled ? (hovered ? 10 : 0) : 20;
	}

	protected int getScrollButtonSpriteY(boolean top, boolean hovered) {
		return isVertical() ? (top ? 0 : 10) : (top ? 20 : 30);
	}
	
	protected int getScrollButtonSpriteWidth() {
		return 30;
	}

	protected int getScrollButtonSpriteHeight() {
		return 40;
	}

	protected boolean isMouseOverScrollButton(int mouseX, int mouseY, boolean top) {
		if (!isMouseOverScrollBar(mouseX, mouseY))
			return false;
		
		int mousePos = isVertical() ? mouseY : mouseX;
		if (top)
			return (mousePos < getButtonHeight());
		
		int length = (isVertical() ? height : width);
		return (mousePos >= length - getButtonHeight());
	}
	
	protected void drawKnobArea() {
		int buttonHeight = getButtonHeight();
		
		if (isVertical()) {
			fill(0, buttonHeight, width, height - buttonHeight, 0xFF000000);
			fill(1, buttonHeight, width - 1, height - buttonHeight, getKnobAreaColor());
		} else {
			fill(buttonHeight, 0, width - buttonHeight, height, 0xFF000000);
			fill(buttonHeight, 1, width - buttonHeight, height - 1, getKnobAreaColor());
		}
	}

	protected void drawKnob(int mouseX, int mouseY, boolean hovered) {
		int k0 = getKnobPos();
		int k1 = k0 + getKnobSize();
		
		int color = getKnobColor(hovered);
		
		if (isVertical()) {
			fill(1, k0, width - 1, k1, color);
		} else {
			fill(k0, 1, k1, height - 1, color);
		}
	}
	
	protected boolean isMouseOverKnob(int mouseX, int mouseY) {
		if (!isMouseOverScrollBar(mouseX, mouseY))
			return false;
		
		int mp = isVertical() ? mouseY : mouseX;

		int kp = getKnobPos();
		return (mp >= kp && mp < kp + getKnobSize());
	}
	
	protected Identifier getScrollButtonTexture() {
		return SCROLL_BUTTON_TEXTURE;
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
	
	protected int getKnobPos() {
		int pos = getButtonHeight();

		int maxScroll = getMaxScrollOffset();
		if (maxScroll > 0) {
			int emptyArea = getKnobAreaSize() - getKnobSize();
			pos += (int)(emptyArea * scrollOffset / maxScroll);
		}

		return pos;
	}
	
	protected int getKnobSize() {
		return Math.max(getKnobAreaSize() * getContentViewSize() / getContentSize(), getMinimumNobSize());
	}

	protected int getKnobAreaSize() {
		return (isVertical() ? height : width) - getButtonHeight() * 2;
	}
	
	protected int getContentSize() {
		return isVertical() ? parent.getContentHeight() : parent.getContentWidth();
	}

	protected int getContentViewSize() {
		return isVertical() ? parent.getContentViewHeight() : parent.getContentViewWidth();
	}

	protected int getButtonWidth() {
		return DEFAULT_BUTTON_WIDTH;
	}

	protected int getButtonHeight() {
		return DEFAULT_BUTTON_HEIGHT;
	}
	
	protected int getMinimumNobSize() {
		return DEFAULT_MINIMUM_NOB_SIZE;
	}

	public int getPreferredScrollBarWidth() {
		return DEFAULT_SCROLL_BAR_WIDTH;
	}
	
	public double getDefaultScrollAmount() {
		return DEFAULT_SCROLL_AMOUNT;
	}
	
	@Override
	public boolean onMouseClickedGS(double mouseX, double mouseY, int button, int mods) {
		if (isMouseOverScrollBar(mouseX, mouseY)) {
			if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_1) {
				double mousePos = isVertical() ? mouseY : mouseX;

				int knobPos = getKnobPos();
				if (mousePos < knobPos) {
					onIncrementalScroll(-1);
				} else if (mousePos >= knobPos + getKnobSize()) {
					onIncrementalScroll(1);
				} else {
					scrollDragActive = true;
				}
			}
			
			return true;
		} else if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			scrollDragActive = false;
		}
		
		return false;
	}
	
	protected boolean isMouseOverScrollBar(double mouseX, double mouseY) {
		return (mouseX >= 0.0 && mouseY >= 0.0 && mouseX < width && mouseY < height);
	}
	
	private void onIncrementalScroll(int sign) {
		double scrollAmount = getIncrementalScroll(sign);
		if (Double.isNaN(scrollAmount) || scrollAmount < 0.0)
			scrollAmount = getDefaultScrollAmount();
		
		setScrollOffset(scrollOffset + sign * scrollAmount);
	}
	
	protected double getIncrementalScroll(int sign) {
		return isVertical() ? parent.getIncrementalScrollY(sign) : parent.getIncrementalScrollX(sign);
	}

	@Override
	public boolean onMouseReleasedGS(double mouseX, double mouseY, int button, int mods) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1)
			scrollDragActive = false;
		return false;
	}
	
	@Override
	public boolean onMouseDraggedGS(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (enabled && button == GLFW.GLFW_MOUSE_BUTTON_1 && scrollDragActive) {
			double delta = isVertical() ? dragY : dragX;

			int compAreaSize = getKnobAreaSize() - getKnobSize();
			if (compAreaSize > 0)
				delta *= (double)getMaxScrollOffset() / compAreaSize;
			
			setScrollOffset(scrollOffset + delta);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onMouseScrolledGS(double mouseX, double mouseY, double scrollX, double scrollY) {
		// In case the user is trying to zoom in or out we should not
		// scroll. This has different behavior on different platforms.
		if (Screen.hasControlDown())
			return false;
		
		// Shift will flip the xScroll and yScroll. This makes it
		// possible to scroll horizontally without any xScroll.
		double actualScroll = (isVertical() != Screen.hasShiftDown()) ? scrollY : scrollX; 

		// Translate into the parent's viewport. See #GSAbstractScrollBar.
		mouseX += x;
		mouseY += y;
		
		if (enabled && mouseX >= 0.0 && mouseY >= 0.0 && mouseX < parent.getWidth() && mouseY < parent.getHeight()) {
			setScrollOffset(scrollOffset - actualScroll * getDefaultScrollAmount());
			
			// Do not suppress further updates
			//return true;
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
	
	protected int getMaxScrollOffset() {
		return Math.max(getContentSize() - getContentViewSize(), 0);
	}
	
	public boolean isVertical() {
		return vertical;
	}
	
	public double getScrollOffset() {
		return scrollOffset;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isScrollDragActive() {
		return scrollDragActive;
	}
}
