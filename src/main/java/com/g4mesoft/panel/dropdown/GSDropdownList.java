package com.g4mesoft.panel.dropdown;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSEPopupPlacement;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSIChangeListener;
import com.g4mesoft.panel.GSILayoutManager;
import com.g4mesoft.panel.GSIModelListener;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSLocation;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSPopup;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.cell.GSCellContext;
import com.g4mesoft.panel.cell.GSCellRendererRegistry;
import com.g4mesoft.panel.cell.GSICellRenderer;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSILayoutEventListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSLayoutEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.panel.scroll.GSIScrollable;
import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;
import com.g4mesoft.util.GSMathUtil;

public class GSDropdownList<T> extends GSPanel implements GSIDropdownListModelListener,
                                                          GSIMouseListener,
                                                          GSIKeyListener {

	/**
	 * The value returned by {@link #getSelectedIndex()} in order to indicate that
	 * the selection of the dropdown list is empty. This value is only returned when
	 * empty selection is permitted.
	 * 
	 * @see #setEmptySelectionAllowed(boolean)
	 */
	public static final int EMPTY_SELECTION = -1;
	
	private static final GSIcon DOWN_ARROW_ICON          = GSPanelContext.getIcon(30, 62, 10, 10);
	private static final GSIcon HOVERED_DOWN_ARROW_ICON  = GSPanelContext.getIcon(40, 62, 10, 10);
	private static final GSIcon DISABLED_DOWN_ARROW_ICON = GSPanelContext.getIcon(50, 62, 10, 10);
	
	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF202020;
	private static final int DEFAULT_HOVERED_BACKGROUND_COLOR = 0xFF2E2E2E;
	private static final int DEFAULT_DISABLED_BACKGROUND_COLOR = 0xFF0A0A0A;
	
	private static final int DEFAULT_TEXT_COLOR = 0xFFE0E0E0;
	private static final int DEFAULT_HOVERED_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DEFAULT_DISABLED_TEXT_COLOR = 0xFF707070;
 
	private static final int DEFAULT_BORDER_WIDTH = 1;
	private static final int DEFAULT_BORDER_COLOR = 0xFF171717;
	private static final int DEFAULT_HOVERED_BORDER_COLOR = 0xFF262626;
	private static final int DEFAULT_DISABLED_BORDER_COLOR = 0xFF060606;
	
	private static final int DEFAULT_VERTICAL_MARGIN   = 2;
	private static final int DEFAULT_HORIZONTAL_MARGIN = 2;

	private static final int DEFAULT_PREFERRED_ROW_COUNT = 5;
	
	private static final int MINIMUM_SELECTED_ITEM_WIDTH = 70;
	private static final int VERTICAL_PADDING = 2;
	
	private GSIDropdownListModel<T> model;
	private boolean preferredFromItems;
	private int selectedIndex;
	private boolean emptySelectionAllowed;
	
	private final GSCellRendererRegistry cellRendererRegistry;
	
	private int backgroundColor;
	private int hoveredBackgroundColor;
	private int disabledBackgroundColor;
	
	private int textColor;
	private int hoveredTextColor;
	private int disabledTextColor;
	
	private GSETextAlignment textAlignment;
	
	private int borderWidth;
	private int borderColor;
	private int hoveredBorderColor;
	private int disabledBorderColor;
	
	private int verticalMargin;
	private int horizontalMargin;
	
	private int preferredItemListRowCount;
	
	private int iconWidth;
	private int selectedItemWidth;

	private GSPopup popup;
	
	private final List<GSIModelListener> modelListeners;
	private final List<GSIChangeListener> changeListeners;
	
	public GSDropdownList() {
		this(new GSBasicDropdownListModel<T>());
	}

	public GSDropdownList(T[] items) {
		this(new GSBasicDropdownListModel<T>(items));
	}

	public GSDropdownList(Collection<T> items) {
		this(new GSBasicDropdownListModel<T>(items));
	}

	public GSDropdownList(GSIDropdownListModel<T> model) {
		preferredFromItems = false;
		selectedIndex = EMPTY_SELECTION;
		emptySelectionAllowed = true;
		
		cellRendererRegistry = new GSCellRendererRegistry();
		
		// Default colors are derived from button.
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		hoveredBackgroundColor = DEFAULT_HOVERED_BACKGROUND_COLOR;
		disabledBackgroundColor = DEFAULT_DISABLED_BACKGROUND_COLOR;
		
		textColor = DEFAULT_TEXT_COLOR;
		hoveredTextColor = DEFAULT_HOVERED_TEXT_COLOR;
		disabledTextColor = DEFAULT_DISABLED_TEXT_COLOR;
		
		textAlignment = GSETextAlignment.LEFT;
		
		borderWidth = DEFAULT_BORDER_WIDTH;
		borderColor = DEFAULT_BORDER_COLOR;
		hoveredBorderColor = DEFAULT_HOVERED_BORDER_COLOR;
		disabledBorderColor = DEFAULT_DISABLED_BORDER_COLOR;

		verticalMargin = DEFAULT_VERTICAL_MARGIN;
		horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
		
		preferredItemListRowCount = DEFAULT_PREFERRED_ROW_COUNT;
		
		modelListeners = new ArrayList<>();
		changeListeners = new ArrayList<>();
				
		setModel(model);
		
		addMouseEventListener(this);
		addKeyEventListener(this);
	}
	
	@Override
	public void add(GSPanel panel) {
		throw new UnsupportedOperationException("Adding panels only allowed internally");
	}

	@Override
	public void remove(GSPanel panel) {
		throw new UnsupportedOperationException("Removing panels only allowed internally");
	}
	
	@Override
	public void layout() {
		int innerWidth = width - borderWidth * 2;
		// Compute the width of the down-arrow icon.
		iconWidth = DOWN_ARROW_ICON.getWidth() + DEFAULT_HORIZONTAL_MARGIN * 2;
		if (innerWidth < iconWidth)
			iconWidth = innerWidth;
		// The remainder of the width is used for the field.
		selectedItemWidth = innerWidth - iconWidth;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		GSCellContext context = new GSCellContext();

		boolean hovered = renderer.isMouseInside(0, 0, width, height);
		if (isEnabled()) {
			context.backgroundColor = hovered ? hoveredBackgroundColor : backgroundColor;
			context.textColor = hovered ? hoveredTextColor : textColor;
		} else {
			context.backgroundColor = disabledBackgroundColor;
			context.textColor = disabledTextColor;
		}
		context.textAlignment = textAlignment;
		
		drawBorderAndBackground(renderer, context, hovered);
		super.render(renderer);

		drawDownArrowIcon(renderer, hovered);
		
		T selectedItem = getSelectedItem();
		if (selectedItem != null)
			drawSelectedItem(renderer, context, selectedItem);
	}
	
	protected void drawBorderAndBackground(GSIRenderer2D renderer, GSCellContext context, boolean hovered) {
		int bc  = isEnabled() ? (hovered ? hoveredBorderColor : borderColor) : disabledBorderColor;

		int bw2 = borderWidth * 2;
		if (borderWidth != 0) {
			// Top, Bottom, Left, Right
			renderer.fillRect(0, 0, width - borderWidth, borderWidth, bc);
			renderer.fillRect(borderWidth, height - borderWidth, width - borderWidth, borderWidth, bc);
			renderer.fillRect(0, borderWidth, borderWidth, height - borderWidth, bc);
			renderer.fillRect(width - borderWidth, 0, borderWidth, height - borderWidth, bc);
		}
		
		if (GSColorUtil.unpackA(context.backgroundColor) != 0x00)
			renderer.fillRect(borderWidth, borderWidth, width - bw2, height - bw2, context.backgroundColor);
	}
	
	protected void drawDownArrowIcon(GSIRenderer2D renderer, boolean hovered) {
		GSIcon icn;
		if (isEnabled()) {
			icn = hovered ? HOVERED_DOWN_ARROW_ICON : DOWN_ARROW_ICON;
		} else {
			icn = DISABLED_DOWN_ARROW_ICON;
		}
		GSRectangle bounds = new GSRectangle();
		bounds.x = width - borderWidth - iconWidth;
		bounds.y = borderWidth;
		bounds.width = iconWidth;
		bounds.height = height - borderWidth * 2;
		icn.render(renderer, bounds);
	}
	
	protected void drawSelectedItem(GSIRenderer2D renderer, GSCellContext context, T selectedItem) {
		context.bounds.x = borderWidth + horizontalMargin;
		context.bounds.y = borderWidth + verticalMargin;
		context.bounds.width = selectedItemWidth;
		context.bounds.height = Math.max(0, height - 2 * context.bounds.y);
		getCellRenderer(selectedItem).render(renderer, selectedItem, context);
	}

	@Override
	public void setLayoutManager(GSILayoutManager layoutManager) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected GSDimension calculatePreferredSize() {
		GSDimension is = preferredItemSize();
		int iw = is.getWidth() + horizontalMargin * 2;
		int ih = is.getHeight() + (verticalMargin + VERTICAL_PADDING) * 2;

		// Compute the icon preferred size.
		int icnW = DOWN_ARROW_ICON.getWidth();
		int icnH = DOWN_ARROW_ICON.getHeight();
		// Use the default margin for the icon.
		icnW += DEFAULT_HORIZONTAL_MARGIN * 2;
		icnH += (DEFAULT_VERTICAL_MARGIN + VERTICAL_PADDING) * 2;
		
		int w = icnW + iw + borderWidth * 2;
		int h = Math.max(icnH, ih) + borderWidth * 2;
		
		return new GSDimension(w, h);
	}
	
	private GSDimension preferredItemSize() {
		int iw = MINIMUM_SELECTED_ITEM_WIDTH, ih = 0;
		if (preferredFromItems) {
			// Compute maximum item minimum-size.
			for (int i = 0, cnt = model.getCount(); i < cnt; i++) {
				T item = model.getItem(i);
				GSICellRenderer<T> renderer = getCellRenderer(item);
				GSDimension mns = renderer.getMinimumSize(item);
				if (mns.getWidth() > iw)
					iw = mns.getWidth();
				if (mns.getHeight() > ih)
					ih = mns.getHeight();
			}
		} else {
			// Compute expected item height (i.e. text height).
			ih = GSPanelContext.getRenderer().getTextHeight();
		}
		return new GSDimension(iw, ih);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (!enabled) {
			// Make sure to close the popup so the user can
			// not make changes while we are disabled.
			closeDropdownPopup();
		}
	}
	
	public GSIDropdownListModel<T> getModel() {
		return model;
	}
	
	public void setModel(GSIDropdownListModel<T> model) {
		if (model == null)
			throw new IllegalArgumentException("model is null!");
		
		if (this.model != null) {
			// Model is null when invoked from the constructor.
			this.model.removeListener(this);
		}
		this.model = model;
		model.addListener(this);

		// Note: reset selection first to ensure we do not have an
		//       invalid selection during the model changed event.
		resetSelection();
		dispatchModelChangedEvent();

		if (preferredFromItems) {
			// Items changed, and thus preferred size changed. We
			// invalidate here to indicate this.
			invalidate();
		}
	}
	
	public boolean isPreferredFromItems() {
		return preferredFromItems;
	}
	
	public void setPreferredFromItems(boolean flag) {
		if (flag != preferredFromItems) {
			preferredFromItems = flag;
			// Preferred size might have changed.
			invalidate();
		}
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}

	public T getSelectedItem() {
		if (selectedIndex == EMPTY_SELECTION)
			return null;
		return model.getItem(selectedIndex);
	}
	
	public void setSelectedIndex(int index) {
		if (index == EMPTY_SELECTION) {
			// Ensure that we allow empty selection
			if (!emptySelectionAllowed)
				throw new IllegalArgumentException("Empty selection not allowed");
		} else {
			if (index < 0 || index >= model.getCount())
				throw new IllegalArgumentException("Selected index out of bounds!");
		}
		if (index != selectedIndex) {
			selectedIndex = index;
			dispatchValueChangedEvent();
		}
	}

	private void resetSelection() {
		setSelectedIndex(emptySelectionAllowed ? EMPTY_SELECTION : 0);
	}
	
	public boolean isEmptySelectionAllowed() {
		return emptySelectionAllowed;
	}

	public void setEmptySelectionAllowed(boolean flag) {
		emptySelectionAllowed = flag;
		if (!emptySelectionAllowed) {
			// Ensure that we do not currently have an empty selection
			if (selectedIndex == EMPTY_SELECTION)
				setSelectedIndex(0);
		}
	}
	
	public GSCellRendererRegistry getCellRendererRegistry() {
		return cellRendererRegistry;
	}
	
	public GSICellRenderer<T> getCellRenderer(T value) {
		return cellRendererRegistry.getCellRenderer(value);
	}
	
	public void setCellRenderer(Class<? extends T> valueClazz, GSICellRenderer<T> cellRenderer) {
		cellRendererRegistry.setCellRenderer(valueClazz, cellRenderer);
	}
	
	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int color) {
		backgroundColor = color;
	}

	public int getHoveredBackgroundColor() {
		return hoveredBackgroundColor;
	}
	
	public void setHoveredBackgroundColor(int color) {
		hoveredBackgroundColor = color;
	}

	public int getDisabledBackgroundColor() {
		return disabledBackgroundColor;
	}
	
	public void setDisabledBackgroundColor(int color) {
		disabledBackgroundColor = color;
	}
	
	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int color) {
		textColor = color;
	}

	public int getHoveredTextColor() {
		return hoveredTextColor;
	}
	
	public void setHoveredTextColor(int color) {
		hoveredTextColor = color;
	}

	public int getDisabledTextColor() {
		return disabledTextColor;
	}
	
	public void setDisabledTextColor(int color) {
		disabledTextColor = color;
	}

	public GSETextAlignment getTextAlignment() {
		return textAlignment;
	}
	
	public void setTextAlignment(GSETextAlignment textAlignment) {
		if (textAlignment == null)
			throw new IllegalArgumentException("textAlignment is null!");
		this.textAlignment = textAlignment;
	}

	public int getBorderWidth() {
		return borderWidth;
	}
	
	public void setBorderWidth(int borderWidth) {
		if (borderWidth < 0)
			throw new IllegalArgumentException("borderWidth must be non-negative!");
		if (borderWidth != this.borderWidth) {
			this.borderWidth = borderWidth;
			invalidate();
		}
	}
	
	public int getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}
	
	public int getHoveredBorderColor() {
		return hoveredBorderColor;
	}
	
	public void setHoveredBorderColor(int hoveredBorderColor) {
		this.hoveredBorderColor = hoveredBorderColor;
	}
	
	public int getDisabledBorderColor() {
		return disabledBorderColor;
	}
	
	public void setDisabledBorderColor(int disabledBorderColor) {
		this.disabledBorderColor = disabledBorderColor;
	}
	
	public int getVerticalMargin() {
		return verticalMargin;
	}

	public void setVerticalMargin(int verticalMargin) {
		if (verticalMargin < 0)
			throw new IllegalArgumentException("verticalMargin must be non-negative!");
		if (verticalMargin != this.verticalMargin) {
			this.verticalMargin = verticalMargin;
			invalidate();
		}
	}

	public int getHorizontalMargin() {
		return horizontalMargin;
	}
	
	public void setHorizontalMargin(int horizontalMargin) {
		if (horizontalMargin < 0)
			throw new IllegalArgumentException("horizontalMargin must be non-negative!");
		if (horizontalMargin != this.horizontalMargin) {
			this.horizontalMargin = horizontalMargin;
			invalidate();
		}
	}
	
	public void setPreferredItemListRowCount(int preferredRowCount) {
		if (preferredRowCount <= 0)
			throw new IllegalArgumentException("preferredRowCount must be positive!");
		this.preferredItemListRowCount = preferredRowCount;
	}
	
	public int getPreferredItemListRowCount() {
		return preferredItemListRowCount;
	}
	
	public void addModelListener(GSIModelListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		modelListeners.add(listener);
	}

	public void removeModelListener(GSIModelListener listener) {
		modelListeners.remove(listener);
	}

	private void dispatchModelChangedEvent() {
		modelListeners.forEach(GSIModelListener::modelChanged);
	}
	
	public void addChangeListener(GSIChangeListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		changeListeners.add(listener);
	}

	public void removeChangeListener(GSIChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	private void dispatchValueChangedEvent() {
		changeListeners.forEach(GSIChangeListener::valueChanged);
	}
	
	@Override
	public void intervalAdded(int i0, int i1) {
		if (selectedIndex >= i0) {
			// Selected item is moved to after the newly added items.
			// Note: no need to invoke value changed listener, since
			//       the selected item is still the same.
			selectedIndex += i1 - i0;
		}
		
		if (preferredFromItems)
			invalidate();
	}

	@Override
	public void intervalRemoved(int i0, int i1) {
		if (selectedIndex >= i1) {
			// Selected item is after the removed items. Adjust the
			// selected index so it matches the new index.
			selectedIndex -= i1 - i0;
		} else if (selectedIndex >= i0) {
			// Selected item is before end index, and after start
			// index. Thus, it is in the range that was removed.
			resetSelection();
		}
		
		if (preferredFromItems)
			invalidate();
	}
	
	@Override
	public void mouseReleased(GSMouseEvent event) {
		if (!isEnabled() || event.getButton() != GSMouseEvent.BUTTON_LEFT) {
			// Only consider events from left mouse button.
			return;
		}
		
		if (popup != null) {
			closeDropdownPopup();
		} else if (isInBounds(x + event.getX(), y + event.getY())) {
			openDropdownPopup();
			event.consume();
		}
	}

	private void openDropdownPopup() {
		if (this.popup == null) {
			GSPanel itemList = new GSItemSelectionList<T>(this);
			GSPopup popup = new GSPopup(new GSScrollPanel(itemList), true);
			popup.show(this, 0, height - borderWidth, GSEPopupPlacement.RELATIVE);
			// Note: the popup attempts to focus the scroll panel, but
			//       we want focus to the item list.
			itemList.requestFocus();
			// Listen for cases where the popup is closed for other some
			// reason. E.g. when losing focus or similar.
			popup.addLayoutEventListener(new GSILayoutEventListener() {
				@Override
				public void panelHidden(GSLayoutEvent event) {
					closeDropdownPopup();
				}
			});
			this.popup = popup;
		}
	}
	
	private void closeDropdownPopup() {
		if (this.popup != null) {
			// Keep local in order to not hide the popup twice.
			GSPopup popup = this.popup;
			this.popup = null;
			popup.hide();
			popup = null;
		}
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (!isEnabled()) {
			// None of the below events should be available when
			// the dropdown list is disabled.
			return;
		}
		
		switch (event.getKeyCode()) {
		case GSKeyEvent.KEY_ENTER:
		case GSKeyEvent.KEY_KP_ENTER:
		case GSKeyEvent.KEY_SPACE:
			openDropdownPopup();
			event.consume();
			break;
		case GSKeyEvent.KEY_HOME:
		case GSKeyEvent.KEY_PAGE_UP:
			resetSelection();
			event.consume();
			break;
		case GSKeyEvent.KEY_END:
		case GSKeyEvent.KEY_PAGE_DOWN:
			if (model.getCount() > 0) {
				setSelectedIndex(model.getCount() - 1);
				event.consume();
			}
			break;
		case GSKeyEvent.KEY_LEFT:
		case GSKeyEvent.KEY_UP:
			if (incrementSelectedIndex(-1))
				event.consume();
			break;
		case GSKeyEvent.KEY_RIGHT:
		case GSKeyEvent.KEY_DOWN:
			if (incrementSelectedIndex(1))
				event.consume();
			break;
		}
	}
	
	private boolean incrementSelectedIndex(int sign) {
		int nIndex = selectedIndex + sign;
		if (nIndex == EMPTY_SELECTION) {
			// Ensure we actually allow empty selections.
			if (emptySelectionAllowed) {
				setSelectedIndex(nIndex);
				return true;
			}
		} else if (nIndex >= 0 && nIndex < model.getCount()) {
			setSelectedIndex(nIndex);
			return true;
		}
		return false;
	}
	
	private static class GSItemSelectionList<T> extends GSPanel implements GSIScrollable,
	                                                                       GSIModelListener,
	                                                                       GSIDropdownListModelListener,
	                                                                       GSIMouseListener,
	                                                                       GSIKeyListener {
		
		private final GSDropdownList<T> dropdown;
		private GSIDropdownListModel<T> model;
		private int selectedIndex;
		
		private GSPopup parentPopup;
		
		public GSItemSelectionList(GSDropdownList<T> dropdown) {
			if (dropdown == null)
				throw new IllegalArgumentException("dropdown is null!");
			this.dropdown = dropdown;
			model = dropdown.getModel();
			selectedIndex = Integer.MIN_VALUE;
			
			parentPopup = null;
		}
		
		@Override
		protected void onShown() {
			super.onShown();

			parentPopup = GSPanelUtil.getPopup(this);
			if (parentPopup == null)
				throw new IllegalArgumentException("Expected a popup grandparent!");
			// Install the mouse and key listeners on the popup panel
			// to avoid issues with stolen focus (e.g. scroll bar).
			parentPopup.addMouseEventListener(this);
			parentPopup.addKeyEventListener(this);

			dropdown.addModelListener(this);
			installModel();
		}
		
		private void installModel() {
			model.addListener(this);
			// The selected index likely changed as well.
			setSelectedIndex(dropdown.getSelectedIndex());
		}

		@Override
		protected void onHidden() {
			super.onHidden();
			
			uninstallModel();
			dropdown.removeModelListener(this);

			//assert(parentPopup != null);
			parentPopup.removeMouseEventListener(this);
			parentPopup.removeKeyEventListener(this);
			parentPopup = null;
		}

		private void uninstallModel() {
			model.removeListener(this);
		}
		
		@Override
		public void render(GSIRenderer2D renderer) {
			super.render(renderer);

			GSRectangle clipBounds = renderer.getClipBounds()
					.intersection(0, 0, width, height);
			
			drawBackground(renderer, clipBounds);
			drawItemList(renderer, clipBounds);
		}
		
		private void drawBackground(GSIRenderer2D renderer, GSRectangle clipBounds) {
			// Note: no need to handle disabled background color, since
			//       this popup UI is never shown when that is the case.
			int color = dropdown.getBackgroundColor();
			if (GSColorUtil.unpackA(color) != 0x00)
				renderer.fillRect(clipBounds.x, clipBounds.y,
						clipBounds.width, clipBounds.height, color);
		}
		
		private void drawItemList(GSIRenderer2D renderer, GSRectangle clipBounds) {
			GSCellContext context = new GSCellContext();
			context.bounds.x = dropdown.getBorderWidth() + dropdown.getHorizontalMargin();
			context.bounds.y = 0;
			context.bounds.width = width - context.bounds.x * 2;
			context.textAlignment = dropdown.getTextAlignment();
			
			int remH = height;
			int i = dropdown.isEmptySelectionAllowed() ? EMPTY_SELECTION : 0;
			for (int cnt = model.getCount(); i < cnt && context.bounds.y < clipBounds.y + clipBounds.height; i++) {
				// Note: EMPTY_SELECTION is -1, so this will result in
				//       dividing by count + 1, when empty is allowed.
				context.bounds.height = remH / (cnt - i);
				remH -= context.bounds.height;
				if (context.bounds.y + context.bounds.height >= clipBounds.y) {
					if (i == selectedIndex || renderer.isMouseInside(context.bounds)) {
						// Draw the selected background
						context.backgroundColor = dropdown.getHoveredBackgroundColor();
						context.textColor = dropdown.getHoveredTextColor();
						renderer.fillRect(0, context.bounds.y, width,
								context.bounds.height, context.backgroundColor);
					} else {
						context.backgroundColor = dropdown.getBackgroundColor();
						context.textColor = dropdown.getTextColor();
					}
					if (i != EMPTY_SELECTION) {
						T item = model.getItem(i);
						dropdown.getCellRenderer(item).render(renderer, item, context);
					}
				}
				context.bounds.y += context.bounds.height;
			}
		}
		
		@Override
		protected GSDimension calculatePreferredSize() {
			return calculatePreferredSize(Integer.MAX_VALUE);
		}
		
		private GSDimension calculatePreferredSize(int rowCount) {
			GSDimension is = dropdown.preferredItemSize();
			// Include the border width and horizontal margin in order to
			// align the items to the selected item shown in the dropdown
			// list that owns this popup item list.
			int iw = is.getWidth() + (dropdown.getBorderWidth() +
					dropdown.getHorizontalMargin()) * 2;
			int cnt = Math.min(rowCount, model.getCount());
			if (dropdown.isEmptySelectionAllowed() && cnt + 1 < rowCount) {
				// Include the empty element.
				cnt++;
			}
			// Every item should receive the same height.
			long hl = (long)(is.getHeight() + VERTICAL_PADDING * 2) * cnt;
			int h = (int)Math.min(Integer.MAX_VALUE, hl);
			return new GSDimension(iw, h);
		}

		private int getItemIndexAtY(int y) {
			int count = model.getCount();
			if (count == 0 || y < 0 || y >= height) {
				// No specific item at location.
				return EMPTY_SELECTION;
			}

			if (dropdown.isEmptySelectionAllowed()) {
				// Also include the empty selection.
				count++;
			}
			// Compute expected item height, and remainder.
			int ih = height / count;
			int rem = height % count;
			
			int index = ih != 0 ? y / ih : 0;
			// Since the last rem items receive exactly one extra pixel in
			// height, we can compute the index after this cutoff with the
			// item height ih + 1.
			if (index >= count - rem) {
				// Bound index at cutoff
				index = count - rem;
				// Compute index offset from cutoff
				index += (y - index * ih) / (ih + 1);
			}

			if (dropdown.isEmptySelectionAllowed()) {
				// First element is the empty selection. Decrement 1 from
				// index to accommodate this.
				index--;
			}
			return index;
		}
		
		public int getItemY(int index) {
			int count = model.getCount();
			int rowIndex = index;
			if (dropdown.isEmptySelectionAllowed()) {
				count++;
				// Convert index to row index.
				rowIndex = index + 1;
			}
			// Return closest pixel to out of bounds indices.
			if (rowIndex <= 0)
				return 0;
			if (rowIndex >= count)
				return height;
			// Compute expected item height, and remainder.
			int ih = height / count;
			int rem = height % count;
			// The first elements all have the same height.
			int y = Math.min(count - rem, rowIndex) * ih;
			// Remaining elements are taller
			if (rowIndex > count - rem)
				y += (rowIndex - (count - rem)) * (ih + 1);
			return y;
		}

		@Override
		public GSDimension getPreferredScrollableSize() {
			int prefRowCount = dropdown.getPreferredItemListRowCount();
			GSDimension ps = calculatePreferredSize(prefRowCount);
			// Attempt to receive the same width as the dropdown.
			int w = Math.max(dropdown.getWidth(), ps.getWidth());
			return new GSDimension(w, ps.getHeight());
		}
		
		@Override
		public boolean isScrollableWidthFilled() {
			return true;
		}
		
		@Override
		public void modelChanged() {
			uninstallModel();
			model = dropdown.getModel();
			installModel();
		}
		
		@Override
		public void intervalAdded(int i0, int i1) {
			invalidate();
		}
		
		@Override
		public void intervalRemoved(int i0, int i1) {
			invalidate();
		}
		
		@Override
		public void mouseReleased(GSMouseEvent event) {
			if (event.getButton() != GSMouseEvent.BUTTON_LEFT) {
				// Only consider events from left mouse button.
				return;
			}
			// Note: event coordinates are in the popup coordinate space.
			GSLocation offset = GSPanelUtil.getRelativeLocation(this, parentPopup);
			int relX = event.getX() - offset.getX();
			int relY = event.getY() - offset.getY();
			// Ensure that the event is in bounds. Since we are listening to
			// events from the popup, this is essential. Note that the
			// location here is relative to the parent.
			if (isInBounds(x + relX, y + relY)) {
				int index = getItemIndexAtY(relY);
				if (dropdown.isEmptySelectionAllowed() || index != EMPTY_SELECTION) {
					setSelectedIndex(index);
					selectAndClose();
					event.consume();
				}
			}
		}
		
		@Override
		public void keyPressed(GSKeyEvent event) {
			switch (event.getKeyCode()) {
			case GSKeyEvent.KEY_ENTER:
			case GSKeyEvent.KEY_KP_ENTER:
			case GSKeyEvent.KEY_SPACE:
				selectAndClose();
				event.consume();
				break;
			case GSKeyEvent.KEY_ESCAPE:
				dropdown.closeDropdownPopup();
				event.consume();
				break;
			case GSKeyEvent.KEY_HOME:
			case GSKeyEvent.KEY_PAGE_UP:
				// Note: will be clamped to 0 if empty selection
				//       is disallowed by the dropdown list.
				setSelectedIndex(EMPTY_SELECTION);
				event.consume();
				break;
			case GSKeyEvent.KEY_END:
			case GSKeyEvent.KEY_PAGE_DOWN:
				setSelectedIndex(model.getCount() - 1);
				event.consume();
				break;
			case GSKeyEvent.KEY_UP:
				setSelectedIndex(selectedIndex - 1);
				event.consume();
				break;
			case GSKeyEvent.KEY_DOWN:
				setSelectedIndex(selectedIndex + 1);
				event.consume();
				break;
			}
		}
		
		private void setSelectedIndex(int index) {
			int oldSelectedIndex = selectedIndex;
			if (dropdown.isEmptySelectionAllowed() && index <= EMPTY_SELECTION) {
				selectedIndex = EMPTY_SELECTION;
			} else {
				selectedIndex = GSMathUtil.clamp(index, 0, model.getCount() - 1);
			}

			if (oldSelectedIndex != selectedIndex) {
				GSRectangle itemBounds = new GSRectangle();
				itemBounds.x = 0;
				itemBounds.width = width;
				itemBounds.y = getItemY(selectedIndex);
				itemBounds.height = getItemY(selectedIndex + 1) - itemBounds.y;
				GSPanelUtil.scrollToVisible(this, itemBounds);
			}
		}
		
		private void selectAndClose() {
			dropdown.setSelectedIndex(selectedIndex);
			dropdown.closeDropdownPopup();
		}
	}
}
