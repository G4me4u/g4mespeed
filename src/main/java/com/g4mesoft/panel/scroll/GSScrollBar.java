package com.g4mesoft.panel.scroll;

import java.util.ArrayList;
import java.util.List;

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

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSScrollBar extends GSPanel implements GSIMouseListener {

	private static final GSITextureRegion SCROLL_BUTTON_TEXTURE = GSPanelContext.getTexture(0, 32, 30, 40);
	
	private static final int KNOB_AREA_COLOR = 0xFF171717;
	private static final int DISABLED_KNOB_AREA_COLOR = 0xFF000000;
	
	private static final int KNOB_COLOR = 0xFF4D4D4D;
	private static final int HOVERED_KNOB_COLOR = 0xFF7A7A7A;
	private static final int DISABLED_KNOB_COLOR = 0xFF2B2A2B;
	
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
	
	private boolean vertical;

	private float minScrollOffset;
	private float maxScrollOffset;
	private float viewSize;
	
	private float scrollOffset;
	private boolean scrollDragActive;

	private final List<GSIScrollListener> scrollListeners;

	public GSScrollBar() {
		this(true, 0.0f, 100.0f, DEFAULT_SCROLL_AMOUNT);
	}
	
	public GSScrollBar(boolean vertical, float minScrollOffset, float maxScrollOffset, float viewSize) {
		this.vertical = vertical;

		this.minScrollOffset = minScrollOffset;
		this.maxScrollOffset = maxScrollOffset;
		this.viewSize = viewSize;
		
		scrollOffset = minScrollOffset;
		scrollDragActive = false;
		
		scrollListeners = new ArrayList<GSIScrollListener>();
		
		addMouseEventListener(this);
	}

	public void addScrollListener(GSIScrollListener listener) {
		scrollListeners.add(listener);
	}

	public void removeScrollListener(GSIScrollListener listener) {
		scrollListeners.remove(listener);
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
		return isEnabled() ? (hovered ? 10 : 0) : 20;
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
		return isEnabled() ? KNOB_AREA_COLOR : DISABLED_KNOB_AREA_COLOR;
	}
	
	protected int getKnobColor(boolean hovered) {
		if (!isEnabled())
			return DISABLED_KNOB_COLOR;
		if (scrollDragActive || hovered)
			return HOVERED_KNOB_COLOR;
		return KNOB_COLOR;
	}
	
	protected int getKnobPos() {
		int pos = getButtonHeight();

		float scrollInterval = maxScrollOffset - minScrollOffset;
		if (scrollInterval > 0.0f) {
			int emptyArea = getKnobAreaSize() - getKnobSize();
			float relativeScroll = scrollOffset - minScrollOffset;
			pos += (int)(emptyArea * relativeScroll / scrollInterval);
		}

		return pos;
	}
	
	protected int getKnobSize() {
		float scrollInterval = viewSize + maxScrollOffset - minScrollOffset;
		if (scrollInterval > viewSize) {
			int knobSize = Math.round(getKnobAreaSize() * viewSize / scrollInterval);
			return Math.max(knobSize, getMinimumNobSize());
		}
		return getKnobAreaSize();
	}

	protected int getKnobAreaSize() {
		return (isVertical() ? height : width) - getButtonHeight() * 2;
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

	@Override
	public void populateRightClickMenu(GSDropdown dropdown, int x, int y) {
		dropdown.addItem(new GSDropdownAction(SCROLL_HERE_TEXT, () -> {
			setScrollOffset(minScrollOffset + getScrollDelta(isVertical() ? y : x) - viewSize * 0.5f);
		}));
		dropdown.separate();
		dropdown.addItem(new GSDropdownAction(isVertical() ? TOP_TEXT : LEFT_EDGE_TEXT, () -> {
			setScrollOffset(minScrollOffset);
		}));
		dropdown.addItem(new GSDropdownAction(isVertical() ? BOTTOM_TEXT : RIGHT_EDGE_TEXT, () -> {
			setScrollOffset(maxScrollOffset);
		}));
		dropdown.separate();
		dropdown.addItem(new GSDropdownAction(isVertical() ? PAGE_UP_TEXT : PAGE_LEFT_TEXT, () -> {
			onPageScroll(-1);
		}));
		dropdown.addItem(new GSDropdownAction(isVertical() ? PAGE_DOWN_TEXT : PAGE_RIGHT_TEXT, () -> {
			onPageScroll(1);
		}));
		dropdown.separate();
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
		if (event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			int mousePos = isVertical() ? event.getY() : event.getX();
			int knobPos = getKnobPos();
			
			if (mousePos < knobPos) {
				if (mousePos < getButtonHeight()) {
					// Incremental scroll when clicking scroll buttons.
					onIncrementalScroll(-1);
				} else {
					// Page scroll if in knob area, but not on the
					// knob itself.
					onPageScroll(-1);
				}
			} else if (mousePos >= knobPos + getKnobSize()) {
				if (mousePos >= height - getButtonHeight()) {
					onIncrementalScroll(1);
				} else {
					onPageScroll(1);
				}
			} else {
				scrollDragActive = true;
			}

			event.consume();
		}
	}
	
	private void onIncrementalScroll(int sign) {
		// TODO: implement incremental scroll
		float scrollAmount = Float.NaN;//getIncrementalScroll(sign);
		if (Float.isNaN(scrollAmount) || scrollAmount < 0.0)
			scrollAmount = DEFAULT_SCROLL_AMOUNT;
		
		setScrollOffset(scrollOffset + sign * scrollAmount);
	}
	
	private void onPageScroll(int sign) {
		setScrollOffset(scrollOffset + sign * viewSize);
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
		if (scrollDragActive && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			float drag = isVertical() ? event.getDragY() : event.getDragX();
			setScrollOffset(scrollOffset + getScrollDelta(drag));
			event.consume();
		}
	}
	
	private float getScrollDelta(float delta) {
		int compAreaSize = getKnobAreaSize() - getKnobSize();
		if (compAreaSize > 0)
			delta *= (maxScrollOffset - minScrollOffset) / compAreaSize;
		return delta;
	}
	
	public boolean isVertical() {
		return vertical;
	}
	
	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}
	
	public float getScrollOffset() {
		return scrollOffset;
	}
	
	public void setScrollOffset(float scroll) {
		for (GSIScrollListener listener : scrollListeners)
			listener.preScrollChanged(scroll);
		
		// Note that minScrollOffset and maxScrollOffset might have
		// changed at this point. Ensure that it changes appropriately.
		scrollOffset = GSMathUtil.clamp(scroll, minScrollOffset, maxScrollOffset);
		
		for (GSIScrollListener listener : scrollListeners)
			listener.scrollChanged(scrollOffset);
	}
	
	public float getMinScrollOffset() {
		return minScrollOffset;
	}
	
	public void setMinScrollOffset(float minScrollOffset) {
		this.minScrollOffset = minScrollOffset;
		
		if (scrollOffset < minScrollOffset)
			setScrollOffset(minScrollOffset);
	}

	public float getMaxScrollOffset() {
		return maxScrollOffset;
	}

	public void setMaxScrollOffset(float maxScrollOffset) {
		this.maxScrollOffset = maxScrollOffset;
		
		if (scrollOffset > maxScrollOffset)
			setScrollOffset(maxScrollOffset);
	}
	
	public float getViewSize() {
		return viewSize;
	}
	
	public void setViewSize(float viewSize) {
		this.viewSize = viewSize;
	}
}
