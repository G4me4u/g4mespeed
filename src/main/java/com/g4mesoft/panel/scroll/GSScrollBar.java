package com.g4mesoft.panel.scroll;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSIChangeListener;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.dropdown.GSDropdown;
import com.g4mesoft.panel.dropdown.GSDropdownAction;
import com.g4mesoft.panel.event.GSFocusEvent;
import com.g4mesoft.panel.event.GSIFocusEventListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSITextureRegion;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSScrollBar extends GSPanel implements GSIMouseListener, GSIFocusEventListener,
                                                    GSIChangeListener, GSIScrollListener {

	private static final GSITextureRegion SCROLL_BUTTON_TEXTURE = GSPanelContext.getTexture(0, 32, 30, 40);
	
	private static final int KNOB_AREA_COLOR = 0xFF171717;
	private static final int DISABLED_KNOB_AREA_COLOR = 0xFF000000;
	
	private static final int KNOB_COLOR = 0xFF4D4D4D;
	private static final int HOVERED_KNOB_COLOR = 0xFF7A7A7A;
	private static final int DISABLED_KNOB_COLOR = 0xFF2B2A2B;
	
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
	
	protected boolean vertical;
	protected GSIScrollBarModel model;
	
	protected boolean scrollDragActive;
	
	protected int viewSize;
	protected int knobAreaSize;
	protected int knobSize;
	protected int knobPos;
	
	protected GSIScrollable scrollable;
	
	private List<GSIScrollListener> scrollListeners;

	public GSScrollBar() {
		this(true);
	}

	public GSScrollBar(boolean vertical) {
		this(vertical, new GSDefaultScrollBarModel());
	}
	
	public GSScrollBar(boolean vertical, float minScroll, float maxScroll) {
		this(vertical, new GSDefaultScrollBarModel(minScroll, maxScroll));
	}

	public GSScrollBar(boolean vertical, GSIScrollBarModel model) {
		this.vertical = vertical;

		scrollDragActive = false;
		
		addMouseEventListener(this);
		addFocusEventListener(this);
		
		setModel(model);
	}
	
	@Override
	protected void onResized(int oldWidth, int oldHeight) {
		updateAttribs();
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
		if (isVertical()) {
			boolean hovered = renderer.isMouseInside(0, knobPos, width, knobSize);
			renderer.fillRect(1, knobPos, width - 2, knobSize, getKnobColor(hovered));
		} else {
			boolean hovered = renderer.isMouseInside(knobPos, 0, knobSize, height);
			renderer.fillRect(knobPos, 1, knobSize, height - 2, getKnobColor(hovered));
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
		float mns = model.getMinScroll();
		float mxs = model.getMaxScroll();
		float vs = viewSize;
		
		dropdown.addItem(new GSDropdownAction(SCROLL_HERE_TEXT, () -> {
			int rel = (isVertical() ? y : x) - getButtonHeight();
			setScroll(mns + getScrollDelta(rel) - vs * 0.5f);
		}));
		dropdown.separate();
		dropdown.addItem(new GSDropdownAction(isVertical() ? TOP_TEXT : LEFT_EDGE_TEXT, () -> {
			setScroll(mns);
		}));
		dropdown.addItem(new GSDropdownAction(isVertical() ? BOTTOM_TEXT : RIGHT_EDGE_TEXT, () -> {
			setScroll(mxs);
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
			
			if (mousePos < knobPos) {
				if (mousePos < getButtonHeight()) {
					// Incremental scroll when clicking scroll buttons.
					onIncrementalScroll(-1);
				} else {
					// Page scroll if in knob area, but not on the
					// knob itself.
					onPageScroll(-1);
				}
			} else if (mousePos >= knobPos + knobSize) {
				int length = isVertical() ? height : width;
				if (mousePos >= length - getButtonHeight()) {
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
		setScroll(model.getScroll() + sign * getIncrementalScroll(sign));
	}
	
	private float getIncrementalScroll(int sign) {
		if (scrollable != null) {
			float incScroll;
			if (vertical) {
				incScroll = scrollable.getIncrementalScrollY(sign);
			} else {
				incScroll = scrollable.getIncrementalScrollX(sign);
			}
			
			// Ensure that the returned scroll is valid (default is NaN)
			if (!Float.isNaN(incScroll) && incScroll > 0.0f)
				return incScroll;
		}
		
		return model.getBlockScroll();
	}
	
	private void onPageScroll(int sign) {
		setScroll(model.getScroll() + sign * viewSize);
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
			setScroll(model.getScroll() + getScrollDelta(drag));
			event.consume();
		}
	}
	
	private float getScrollDelta(float delta) {
		int complementaryAreaSize = knobAreaSize - knobSize;
		if (complementaryAreaSize > 0) {
			float scrollInterval = model.getMaxScroll() - model.getMinScroll();
			delta *= scrollInterval / complementaryAreaSize;
		}
		return delta;
	}
	
	@Override
	public void focusLost(GSFocusEvent event) {
		scrollDragActive = false;
	}
	
	public boolean isVertical() {
		return vertical;
	}
	
	public void setVertical(boolean vertical) {
		this.vertical = vertical;
	}
	
	public float getScroll() {
		return model.getScroll();
	}
	
	public boolean setScroll(float scroll) {
		return model.setScroll(scroll);
	}
	
	public GSIScrollBarModel getModel() {
		return model;
	}
	
	public void setModel(GSIScrollBarModel model) {
		if (model == null)
			throw new IllegalArgumentException("model is null!");

		GSIScrollBarModel oldModel = this.model;
		this.model = model;

		if (oldModel != null) {
			float oldScroll = oldModel.getScroll();
			oldModel.removeChangeListener(this);
			oldModel.removeScrollListener(this);
			model.setScroll(oldScroll);
		}
		
		model.addChangeListener(this);
		model.addScrollListener(this);

		updateAttribs();
	}

	public GSIScrollable getScrollable() {
		return scrollable;
	}
	
	public void setScrollable(GSIScrollable scrollable) {
		this.scrollable = scrollable;
	}
	
	public void addScrollListener(GSIScrollListener listener) {
		if (scrollListeners == null)
			scrollListeners = new ArrayList<>();
		scrollListeners.add(listener);
	}

	public void removeScrollListener(GSIScrollListener listener) {
		if (scrollListeners != null)
			scrollListeners.remove(listener);
	}
	
	private void dispatchScrollChanged(float newScroll) {
		if (scrollListeners != null) {
			for (GSIScrollListener listener : scrollListeners)
				listener.scrollChanged(newScroll);
		}
	}

	@Override
	public void valueChanged() {
		updateAttribs();
	}
	
	private void updateAttribs() {
		// Assume view size is the same as the scroll bar size in the
		// respective dimension.
		viewSize = isVertical() ? height : width;
		knobAreaSize = viewSize - getButtonHeight() * 2;
		// Calculate initial offset for knob pos (offset later).
		knobPos = getButtonHeight();
		
		float interval = model.getMaxScroll() - model.getMinScroll();
		if (interval > 0.0f) {
			// Calculate the knob size
			float nSize = viewSize / (viewSize + interval);
			knobSize = Math.round(knobAreaSize * nSize);
			if (getMinimumNobSize() > knobSize)
				knobSize = getMinimumNobSize();
			
			// Calculate the knob position
			int emptyArea = knobAreaSize - knobSize;
			float relScroll = model.getScroll() - model.getMinScroll();
			knobPos += Math.round(emptyArea * relScroll / interval);
		} else {
			knobSize = knobAreaSize;
		}
	}
	
	@Override
	public void scrollChanged(float newScroll) {
		dispatchScrollChanged(newScroll);
	}
}
