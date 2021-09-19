package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.event.GSILayoutEventListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSLayoutEvent;
import com.g4mesoft.panel.event.GSMouseEvent;

import net.minecraft.client.gui.screen.Screen;

public class GSScrollPanel extends GSParentPanel implements GSIMouseListener, GSIScrollListener {

	private final GSViewport contentViewport;
	private final GSViewport columnHeaderViewport;
	private final GSViewport rowHeaderViewport;
	
	private GSScrollBar verticalScrollBar;
	private GSScrollBar horizontalScrollBar;
	
	private GSPanel topLeftCorner;
	private GSPanel topRightCorner;
	private GSPanel bottomLeftCorner;
	private GSPanel bottomRightCorner;
	
	private GSEScrollBarPolicy verticalScrollBarPolicy;
	private GSEScrollBarPolicy horizontalScrollBarPolicy;
	
	private final GSContentLayoutListener contentLayoutListener;

	public GSScrollPanel() {
		this(null);
	}
	
	public GSScrollPanel(GSPanel content) {
		contentViewport = new GSViewport();
		columnHeaderViewport = new GSViewport();
		rowHeaderViewport = new GSViewport();

		verticalScrollBarPolicy = GSEScrollBarPolicy.SCROLLBAR_AS_NEEDED;
		horizontalScrollBarPolicy = GSEScrollBarPolicy.SCROLLBAR_AS_NEEDED;

		contentLayoutListener = new GSContentLayoutListener();
		
		setVerticalScrollBar(new GSScrollBar());
		setHorizontalScrollBar(new GSScrollBar());

		setContent(content);
		setBottomRightCorner(new GSScrollPanelCorner());

		setLayoutManager(new GSScrollPanelLayoutManager());
	
		setFocusable(false);
		addMouseEventListener(this);
	}

	/* Visible for GSScrollPanelLayoutManager */
	GSViewport getContentViewport() {
		return contentViewport;
	}
	
	public GSPanel getContent() {
		return contentViewport.getContent();
	}

	public void setContent(GSPanel content) {
		GSPanel oldContent = contentViewport.getContent();
		if (oldContent != null)
			oldContent.removeLayoutEventListener(contentLayoutListener);
		
		contentViewport.setContent(content);

		if (content != null)
			content.addLayoutEventListener(contentLayoutListener);
		
		invalidate();
	}

	/* Visible for GSScrollPanelLayoutManager */
	GSViewport getColumnHeaderViewport() {
		return columnHeaderViewport;
	}

	public GSPanel getColumnHeader() {
		return columnHeaderViewport.getContent();
	}

	public void setColumnHeader(GSPanel columnHeader) {
		GSPanel oldColumnHeader = columnHeaderViewport.getContent();
		if (oldColumnHeader != null)
			oldColumnHeader.removeLayoutEventListener(contentLayoutListener);
		
		columnHeaderViewport.setContent(columnHeader);

		if (columnHeader != null)
			columnHeader.addLayoutEventListener(contentLayoutListener);
		
		if (columnHeaderViewport.isAdded()) {
			if (columnHeader != null)
				add(columnHeaderViewport);
		} else {
			if (columnHeader == null)
				remove(columnHeaderViewport);
		}

		invalidate();
	}

	/* Visible for GSScrollPanelLayoutManager */
	GSViewport getRowHeaderViewport() {
		return rowHeaderViewport;
	}
	
	public GSPanel getRowHeader() {
		return rowHeaderViewport.getContent();
	}

	public void setRowHeader(GSPanel rowHeader) {
		GSPanel oldRowHeader = rowHeaderViewport.getContent();
		if (oldRowHeader != null)
			oldRowHeader.removeLayoutEventListener(contentLayoutListener);
		
		rowHeaderViewport.setContent(rowHeader);

		if (rowHeader != null)
			rowHeader.addLayoutEventListener(contentLayoutListener);

		if (rowHeaderViewport.isAdded()) {
			if (rowHeader != null)
				add(rowHeaderViewport);
		} else {
			if (rowHeader == null)
				remove(rowHeaderViewport);
		}
		
		invalidate();
	}
	
	public GSScrollBar getVerticalScrollBar() {
		return verticalScrollBar;
	}
	
	public void setVerticalScrollBar(GSScrollBar scrollBar) {
		if (scrollBar == null)
			throw new IllegalArgumentException("scrollBar is null");
		if (scrollBar.isAdded())
			throw new IllegalArgumentException("scrollBar already has a parent");
		
		// verticalScrollBar is null when called from constructor
		if (verticalScrollBar != null) {
			if (verticalScrollBar.isAdded())
				remove(verticalScrollBar);
			verticalScrollBar.getModel().removeScrollListener(this);
		}
		
		verticalScrollBar = scrollBar;
		verticalScrollBar.setModel(new GSContentScrollBarModel(true));
		verticalScrollBar.getModel().addScrollListener(this);
		verticalScrollBar.setVertical(true);
		
		updateScrollBarParams();
		
		invalidate();
	}

	public GSScrollBar getHorizontalScrollBar() {
		return horizontalScrollBar;
	}
	
	public void setHorizontalScrollBar(GSScrollBar scrollBar) {
		if (scrollBar == null)
			throw new IllegalArgumentException("scrollBar is null");
		if (scrollBar.isAdded())
			throw new IllegalArgumentException("scrollBar already has a parent");

		// horizontalScrollBar is null when called from constructor
		if (horizontalScrollBar != null) {
			if (horizontalScrollBar.isAdded())
				remove(horizontalScrollBar);
			horizontalScrollBar.getModel().removeScrollListener(this);
		}
		
		horizontalScrollBar = scrollBar;
		horizontalScrollBar.setModel(new GSContentScrollBarModel(false));
		horizontalScrollBar.getModel().addScrollListener(this);
		horizontalScrollBar.setVertical(false);
		
		updateScrollBarParams();
		
		invalidate();
	}

	public GSPanel getTopLeftCorner() {
		return topLeftCorner;
	}

	public void setTopLeftCorner(GSPanel panel) {
		if (panel.isAdded())
			throw new IllegalArgumentException("panel already has a parent");
	
		if (topLeftCorner != null && topLeftCorner.isAdded())
			remove(topLeftCorner);
	
		topLeftCorner = panel;
		
		invalidate();
	}

	public GSPanel getTopRightCorner() {
		return topRightCorner;
	}
	
	public void setTopRightCorner(GSPanel panel) {
		if (panel.isAdded())
			throw new IllegalArgumentException("panel already has a parent");
		
		if (topRightCorner != null && topRightCorner.isAdded())
			remove(topRightCorner);
		
		topRightCorner = panel;

		invalidate();
	}

	public GSPanel getBottomLeftCorner() {
		return bottomLeftCorner;
	}
	
	public void setBottomLeftCorner(GSPanel panel) {
		if (panel.isAdded())
			throw new IllegalArgumentException("panel already has a parent");
		
		if (bottomLeftCorner != null && bottomLeftCorner.isAdded())
			remove(bottomLeftCorner);
		
		bottomLeftCorner = panel;
		
		invalidate();
	}
	
	public GSPanel getBottomRightCorner() {
		return bottomRightCorner;
	}
	
	public void setBottomRightCorner(GSPanel panel) {
		if (panel.isAdded())
			throw new IllegalArgumentException("panel already has a parent");
		
		if (bottomRightCorner != null && bottomRightCorner.isAdded())
			remove(bottomRightCorner);
		
		bottomRightCorner = panel;
		
		invalidate();
	}

	public GSEScrollBarPolicy getVerticalScrollBarPolicy() {
		return verticalScrollBarPolicy;
	}
	
	public void setVerticalScrollBarPolicy(GSEScrollBarPolicy scrollBarPolicy) {
		if (scrollBarPolicy == null)
			throw new IllegalArgumentException("scrollBarPolicy is null");
		
		if (scrollBarPolicy != verticalScrollBarPolicy) {
			verticalScrollBarPolicy = scrollBarPolicy;
			
			invalidate();
		}
	}
	
	public GSEScrollBarPolicy getHorizontalScrollBarPolicy() {
		return horizontalScrollBarPolicy;
	}

	public void setHorizontalScrollBarPolicy(GSEScrollBarPolicy scrollBarPolicy) {
		if (scrollBarPolicy == null)
			throw new IllegalArgumentException("scrollBarPolicy is null");
		
		if (scrollBarPolicy != horizontalScrollBarPolicy) {
			horizontalScrollBarPolicy = scrollBarPolicy;
			
			invalidate();
		}
	}
	
	@Override
	public void mouseScrolled(GSMouseEvent event) {
		// In case the user is trying to zoom in or out we should not
		// scroll. This has different behavior on different platforms.
		if (!Screen.hasControlDown() && !Screen.hasAltDown()) {
			float newScrollX = horizontalScrollBar.getScroll();
			float newScrollY = verticalScrollBar.getScroll();
			
			float blockScrollX = horizontalScrollBar.getModel().getBlockScroll();
			float blockScrollY = verticalScrollBar.getModel().getBlockScroll();

			// Shift will flip the xScroll and yScroll. This makes it
			// possible to scroll horizontally without any xScroll.
			if (Screen.hasShiftDown()) {
				newScrollX -= event.getScrollY() * blockScrollX;
				newScrollY -= event.getScrollX() * blockScrollY;
			} else {
				newScrollX -= event.getScrollX() * blockScrollX;
				newScrollY -= event.getScrollY() * blockScrollY;
			}
			
			if (!horizontalScrollBar.isAdded())
				newScrollX = 0.0f;
			if (!verticalScrollBar.isAdded())
				newScrollY = 0.0f;

			int mx = event.getX();
			int my = event.getY();
			
			if (!columnHeaderViewport.isEmpty() && columnHeaderViewport.isInBounds(mx, my)) {
				// The user is hovering over the column header. Ignore
				// scrollY to make scrolling more intuitive.
				horizontalScrollBar.setScroll(newScrollX);
			} else if (!rowHeaderViewport.isEmpty() && rowHeaderViewport.isInBounds(mx, my)) {
				// Similarly, ignore scrollX when hovering the row header.
				verticalScrollBar.setScroll(newScrollY);
			} else {
				horizontalScrollBar.setScroll(newScrollX);
				verticalScrollBar.setScroll(newScrollY);
			}
		}
	}

	@Override
	public void scrollChanged(float ignore) {
		// Called from both the vertical and horizontal scroll bar,
		// so we have to ignore the newScrollOffset from parameters.
		int offsetX = Math.round(-horizontalScrollBar.getScroll());
		int offsetY = Math.round(-verticalScrollBar.getScroll());
		// Update the offsets of the viewports.
		contentViewport.setOffset(offsetX, offsetY);
		columnHeaderViewport.setOffset(offsetX, 0);
		rowHeaderViewport.setOffset(0, offsetY);
	}
	
	private void updateScrollBarParams() {
		// Calculate the minimum and maximum scroll
		float maxScrollX = 0.0f;
		float maxScrollY = 0.0f;

		GSPanel content = getContent();
		if (content != null) {
			maxScrollX = content.getWidth();
			maxScrollY = content.getHeight();
		}
		GSPanel columnHeader = getColumnHeader();
		if (columnHeader != null && columnHeader.getWidth() > maxScrollX)
			maxScrollX = columnHeader.getWidth();
		GSPanel rowHeader = getRowHeader();
		if (rowHeader != null && rowHeader.getHeight() > maxScrollY)
			maxScrollY = rowHeader.getHeight();
		
		// Subtract the already visible viewport
		maxScrollX -= contentViewport.getWidth();
		maxScrollY -= contentViewport.getHeight();
		
		if (maxScrollX < 0.0f)
			maxScrollX = 0.0f;
		if (maxScrollY < 0.0f)
			maxScrollY = 0.0f;
		
		// Update minimum, maximum and view parameters of the scroll bar
		if (verticalScrollBar != null) {
			GSIScrollBarModel model = verticalScrollBar.getModel();
			model.setScrollInterval(0.0f, maxScrollY);
		}
		
		if (horizontalScrollBar != null) {
			GSIScrollBarModel model = horizontalScrollBar.getModel();
			model.setScrollInterval(0.0f, maxScrollX);
		}
	}
	
	public int getViewportOffsetX() {
		// Both column header and content should have the same offsetX,
		// so it is sufficient to return the content offset.
		return contentViewport.getOffsetX();
	}

	public int getViewportOffsetY() {
		// Both row header and content should have the same offsetY,
		// so it is sufficient to return the content offset.
		return contentViewport.getOffsetY();
	}
	
	private class GSContentScrollBarModel extends GSDefaultScrollBarModel {
		
		private boolean vertical;
		
		public GSContentScrollBarModel(boolean vertical) {
			this.vertical = vertical;
		}
		
		@Override
		public float getIncrementalScroll(int sign) {
			GSPanel content = getContent();
			if (content instanceof GSIScrollable) {
				GSIScrollable scrollable = (GSIScrollable)content;
				
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
			
			return super.getIncrementalScroll(sign);
		}
	}
	
	private class GSContentLayoutListener implements GSILayoutEventListener {
		
		@Override
		public void panelResized(GSLayoutEvent event) {
			updateScrollBarParams();
		}
	}
}
