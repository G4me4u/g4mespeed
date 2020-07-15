package com.g4mesoft.gui.scroll;

import com.g4mesoft.gui.GSIElement;
import com.g4mesoft.gui.GSPanel;
import com.g4mesoft.gui.event.GSIMouseListener;
import com.g4mesoft.gui.event.GSMouseEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.gui.renderer.GSTexture;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

public class GSScrollBar extends GSPanel implements GSIMouseListener {

	private static final Identifier TEXTURE_IDENTIFIER = new Identifier("g4mespeed/textures/scroll_bar.png");
	private static final GSTexture SCROLL_BUTTON_TEXTURE = new GSTexture(TEXTURE_IDENTIFIER, 30, 40);
	
	private static final int KNOB_AREA_COLOR = 0xFF7F7F7F;
	private static final int DISABLED_KNOB_AREA_COLOR = 0xFF595959;

	private static final int KNOB_COLOR = 0xFFC6C6C6;
	private static final int HOVERED_KNOB_COLOR = 0xFFFFFFFF;
	private static final int DISABLED_KNOB_COLOR = 0xFF7F7F7F;
	
	private static final float DEFAULT_SCROLL_AMOUNT = 20.0f;
	
	private static final int DEFAULT_SCROLL_BAR_WIDTH = 9;
	private static final int DEFAULT_BUTTON_WIDTH = 9;
	private static final int DEFAULT_BUTTON_HEIGHT = 10;
	private static final int DEFAULT_MINIMUM_NOB_SIZE = 10;
	
	protected final GSIScrollableViewport parent;
	private final GSIScrollListener listener;
	
	private final GSParentScrollHandler parentScrollHandler;
	
	private boolean vertical;

	private boolean scrollDragActive;
	private float scrollOffset;
	
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
		
		parentScrollHandler = new GSParentScrollHandler();

		// Vertical by default
		vertical = true;
		
		enabled = true;
		
		addMouseEventListener(this);
	}

	public void initVerticalLeft(int xl, int yt, int height) {
		setBounds(xl, yt, getPreferredScrollBarWidth(), height);
		
		vertical = true;
	}

	public void initVerticalRight(int xr, int yt, int height) {
		initVerticalLeft(xr - getPreferredScrollBarWidth(), yt, height);
	}
	
	public void initHorizontalTop(int xl, int yt, int width) {
		setBounds(xl, yt, width, getPreferredScrollBarWidth());

		vertical = false;
	}

	public void initHorizontalBottom(int xl, int yb, int width) {
		initHorizontalTop(xl, yb - getPreferredScrollBarWidth(), width);
	}
	
	@Override
	public void onBoundsChanged() {
		super.onBoundsChanged();

		// Update the scroll offset to ensure that it is valid.
		setScrollOffset(scrollOffset);
	}
	
	@Override
	public void onAdded(GSIElement parent) {
		super.onAdded(parent);

		parent.addMouseEventListener(parentScrollHandler);
	}
	
	@Override
	public void onRemoved(GSIElement parent) {
		super.onRemoved(parent);

		parent.removeMouseEventListener(parentScrollHandler);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		drawScrollButton(renderer, true);
		drawScrollButton(renderer, false);
		
		drawKnobArea(renderer);
		drawKnob(renderer);
	}
	
	protected void drawScrollButton(GSIRenderer2D renderer, boolean top) {
		int bp = top ? 0 : (isVertical() ? height : width) - getButtonHeight();

		if (isVertical()) {
			drawScrollButton(renderer, 0, bp, getButtonWidth(), getButtonHeight(), top);
		} else {
			drawScrollButton(renderer, bp, 0, getButtonHeight(), getButtonWidth(), top);
		}
	}
	
	protected void drawScrollButton(GSIRenderer2D renderer, int bx, int by, int bw, int bh, boolean top) {
		boolean hovered = renderer.isMouseInside(bx, by, bw, bh);
		
		int sx = getScrollButtonSpriteX(top, hovered);
		int sy = getScrollButtonSpriteY(top, hovered);
		
		renderer.drawTexture(getScrollButtonTexture(), bx, by, bw, bh, sx, sy);
	}

	protected int getScrollButtonSpriteX(boolean top, boolean hovered) {
		return enabled ? (hovered ? 10 : 0) : 20;
	}

	protected int getScrollButtonSpriteY(boolean top, boolean hovered) {
		return isVertical() ? (top ? 0 : 10) : (top ? 20 : 30);
	}
	
	protected boolean isMouseOverScrollButton(int mouseX, int mouseY, boolean top) {
		int mousePos = isVertical() ? mouseY : mouseX;
		if (top)
			return (mousePos < getButtonHeight());
		
		int length = (isVertical() ? height : width);
		return (mousePos >= length - getButtonHeight());
	}
	
	protected void drawKnobArea(GSIRenderer2D renderer) {
		int bh = getButtonHeight();
		
		if (isVertical()) {
			renderer.fillRect(0, bh, width    , height - 2 * bh, 0xFF000000);
			renderer.fillRect(1, bh, width - 2, height - 2 * bh, getKnobAreaColor());
		} else {
			renderer.fillRect(bh, 0, width - 2 * bh, height    , 0xFF000000);
			renderer.fillRect(bh, 1, width - 2 * bh, height - 2, getKnobAreaColor());
		}
	}

	protected void drawKnob(GSIRenderer2D renderer) {
		int kp = getKnobPos();
		int ks = getKnobSize();
		
		if (isVertical()) {
			boolean hovered = renderer.isMouseInside(0, kp, width, ks);
			renderer.fillRect(1, kp, width - 2, ks, getKnobColor(hovered));
		} else {
			boolean hovered = renderer.isMouseInside(kp, 0, ks, height);
			renderer.fillRect(kp, 1, ks, height - 2, getKnobColor(hovered));
		}
	}
	
	protected GSTexture getScrollButtonTexture() {
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
		int contentSize = getContentSize();
		int visibleContent = Math.min(getContentViewSize(), contentSize);
		return Math.max(getKnobAreaSize() * visibleContent / contentSize, getMinimumNobSize());
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
	
	public float getDefaultScrollAmount() {
		return DEFAULT_SCROLL_AMOUNT;
	}
	
	@Override
	public void mousePressed(GSMouseEvent event) {
		if (enabled && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			int mousePos = isVertical() ? event.getY() : event.getX();

			int knobPos = getKnobPos();
			if (mousePos < knobPos) {
				onIncrementalScroll(-1);
			} else if (mousePos >= knobPos + getKnobSize()) {
				onIncrementalScroll(1);
			} else {
				scrollDragActive = true;
			}

			event.consume();
		}
	}
	
	private void onIncrementalScroll(int sign) {
		float scrollAmount = getIncrementalScroll(sign);
		if (Float.isNaN(scrollAmount) || scrollAmount < 0.0)
			scrollAmount = getDefaultScrollAmount();
		
		setScrollOffset(scrollOffset + sign * scrollAmount);
	}
	
	protected float getIncrementalScroll(int sign) {
		return isVertical() ? parent.getIncrementalScrollY(sign) : parent.getIncrementalScrollX(sign);
	}

	@Override
	public void mouseReleased(GSMouseEvent event) {
		if (event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			scrollDragActive = false;
			event.consume();
		}
	}
	
	@Override
	public void mouseDragged(GSMouseEvent event) {
		if (enabled && event.getButton() == GSMouseEvent.BUTTON_LEFT && scrollDragActive) {
			float delta = isVertical() ? event.getDragY() : event.getDragX();

			int compAreaSize = getKnobAreaSize() - getKnobSize();
			if (compAreaSize > 0)
				delta *= (float)getMaxScrollOffset() / compAreaSize;
			
			setScrollOffset(scrollOffset + delta);
			
			event.consume();
		}
	}
	
	public void setScrollOffset(float scroll) {
		if (listener != null)
			listener.preScrollChanged(scroll);
		
		scrollOffset = GSMathUtils.clamp(scroll, 0.0f, getMaxScrollOffset());
		
		if (listener != null)
			listener.scrollChanged(scrollOffset);
	}
	
	protected int getMaxScrollOffset() {
		return Math.max(getContentSize() - getContentViewSize(), 0);
	}
	
	public boolean isVertical() {
		return vertical;
	}
	
	public float getScrollOffset() {
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
	
	private class GSParentScrollHandler implements GSIMouseListener {
		
		@Override
		public void mouseScrolled(GSMouseEvent event) {
			// In case the user is trying to zoom in or out we should not
			// scroll. This has different behavior on different platforms.
			if (!Screen.hasControlDown() && !Screen.hasAltDown()) {
				float scroll;

				// Shift will flip the xScroll and yScroll. This makes it
				// possible to scroll horizontally without any xScroll.
				if (isVertical() != Screen.hasShiftDown()) {
					scroll = event.getScrollY(); 
				} else {
					scroll = event.getScrollX();
				}
			
				int mx = event.getX();
				int my = event.getY();
				
				if (enabled && mx >= 0.0 && my >= 0.0 && mx < parent.getWidth() && my < parent.getHeight()) {
					setScrollOffset(scrollOffset - scroll * getDefaultScrollAmount());
					event.consume();
				}
			}
		}
	}
}
