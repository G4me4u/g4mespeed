package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.dropdown.GSDropdown;
import com.g4mesoft.panel.dropdown.GSDropdownAction;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSITextureRegion;
import com.g4mesoft.util.GSMathUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSScrollBar extends GSPanel implements GSIMouseListener {

	private static final GSITextureRegion SCROLL_BUTTON_TEXTURE = GSPanelContext.getTexture(0, 32, 30, 40);
	
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
	
	private static final Text SCROLL_HERE_TEXT  = new TranslatableText("panel.scrollbar.scrollhere");
	private static final Text TOP_TEXT          = new TranslatableText("panel.scrollbar.top");
	private static final Text BOTTOM_TEXT       = new TranslatableText("panel.scrollbar.bottom");
	private static final Text PAGE_UP_TEXT      = new TranslatableText("panel.scrollbar.pageup");
	private static final Text PAGE_DOWN_TEXT    = new TranslatableText("panel.scrollbar.pagedown");
	private static final Text SCROLL_UP_TEXT    = new TranslatableText("panel.scrollbar.scrollup");
	private static final Text SCROLL_DOWN_TEXT  = new TranslatableText("panel.scrollbar.scrolldown");
	private static final Text LEFT_EDGE_TEXT    = new TranslatableText("panel.scrollbar.leftedge");
	private static final Text RIGHT_EDGE_TEXT   = new TranslatableText("panel.scrollbar.rightedge");
	private static final Text PAGE_LEFT_TEXT    = new TranslatableText("panel.scrollbar.pageleft");
	private static final Text PAGE_RIGHT_TEXT   = new TranslatableText("panel.scrollbar.pageright");
	private static final Text SCROLL_LEFT_TEXT  = new TranslatableText("panel.scrollbar.scrollleft");
	private static final Text SCROLL_RIGHT_TEXT = new TranslatableText("panel.scrollbar.scrollright");
	
	protected final GSIScrollable parent;
	private final GSIScrollListener listener;
	
	private final GSParentScrollHandler parentScrollHandler;
	
	private boolean vertical;

	private boolean scrollDragActive;
	private float scrollOffset;
	
	private boolean enabled;
	
	/**
	 * <b>NOTE:</b> parent must be the GSParentPanel that dispatches events to this
	 * scroll bar. If this is not the case, mouse scrolling might have undefined
	 * behavior.
	 * 
	 * @param parent
	 * @param listener
	 */
	public GSScrollBar(GSIScrollable parent, GSIScrollListener listener) {
		this.parent = parent;
		this.listener = listener;
		
		parentScrollHandler = new GSParentScrollHandler();

		// Vertical by default
		setVertical(true);
		
		enabled = true;
		
		addMouseEventListener(this);
	}

	@Override
	public void onBoundsChanged() {
		super.onBoundsChanged();

		// Update the scroll offset to ensure that it is valid.
		setScrollOffset(scrollOffset);
	}
	
	@Override
	public void onAdded(GSPanel parent) {
		super.onAdded(parent);

		parent.addMouseEventListener(parentScrollHandler);
	}
	
	@Override
	public void onRemoved(GSPanel parent) {
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
	
	protected GSITextureRegion getScrollButtonTexture() {
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
		if (contentSize > 0) {
			int visibleContent = Math.min(getContentViewSize(), contentSize);
			return Math.max(getKnobAreaSize() * visibleContent / contentSize, getMinimumNobSize());
		}

		return getKnobAreaSize();
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

	public float getDefaultScrollAmount() {
		return DEFAULT_SCROLL_AMOUNT;
	}
	
	@Override
	public void createRightClickMenu(GSDropdown dropdown, int x, int y) {
		dropdown.addItemSeparator();
		dropdown.addItem(new GSDropdownAction(SCROLL_HERE_TEXT, () -> {
			setScrollOffset(getScrollDelta(isVertical() ? y : x) - getContentViewSize() * 0.5f);
		}));
		dropdown.addItemSeparator();
		dropdown.addItem(new GSDropdownAction(isVertical() ? TOP_TEXT : LEFT_EDGE_TEXT, () -> {
			setScrollOffset(0.0f);
		}));
		dropdown.addItem(new GSDropdownAction(isVertical() ? BOTTOM_TEXT : RIGHT_EDGE_TEXT, () -> {
			setScrollOffset(getMaxScrollOffset());
		}));
		dropdown.addItemSeparator();
		dropdown.addItem(new GSDropdownAction(isVertical() ? PAGE_UP_TEXT : PAGE_LEFT_TEXT, () -> {
			onPageScroll(-1);
		}));
		dropdown.addItem(new GSDropdownAction(isVertical() ? PAGE_DOWN_TEXT : PAGE_RIGHT_TEXT, () -> {
			onPageScroll(1);
		}));
		dropdown.addItemSeparator();
		dropdown.addItem(new GSDropdownAction(isVertical() ? SCROLL_UP_TEXT : SCROLL_LEFT_TEXT, () -> {
			onIncrementalScroll(-1);
		}));
		dropdown.addItem(new GSDropdownAction(isVertical() ? SCROLL_DOWN_TEXT : SCROLL_RIGHT_TEXT, () -> {
			onIncrementalScroll(1);
		}));
	}
	
	@Override
	protected GSDimension calculatePreferredSize() {
		int w = Math.max(DEFAULT_SCROLL_BAR_WIDTH, getButtonWidth());
		int h = getMinimumNobSize() + getButtonHeight() * 2;
		return isVertical() ? new GSDimension(w, h) : new GSDimension(h, w);
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
	
	private void onPageScroll(int sign) {
		setScrollOffset(scrollOffset + sign * getContentViewSize());
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
			float drag = isVertical() ? event.getDragY() : event.getDragX();
			setScrollOffset(scrollOffset + getScrollDelta(drag));
			event.consume();
		}
	}
	
	private float getScrollDelta(float delta) {
		int compAreaSize = getKnobAreaSize() - getKnobSize();
		if (compAreaSize > 0)
			delta *= (float)getMaxScrollOffset() / compAreaSize;
		return delta;
	}
	
	public void setScrollOffset(float scroll) {
		if (listener != null)
			listener.preScrollChanged(scroll);
		
		scrollOffset = GSMathUtil.clamp(scroll, 0.0f, getMaxScrollOffset());
		
		if (listener != null)
			listener.scrollChanged(scrollOffset);
	}
	
	protected int getMaxScrollOffset() {
		return Math.max(getContentSize() - getContentViewSize(), 0);
	}
	
	public boolean isVertical() {
		return vertical;
	}
	
	public void setVertical(boolean vertical) {
		if (vertical != this.vertical) {
			this.vertical = vertical;
			
			GSPanel parent = getParent();
			if (parent != null)
				parent.requestLayout();
		}
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
