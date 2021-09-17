package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;

import net.minecraft.client.gui.screen.Screen;

public class GSScrollPanel extends GSParentPanel implements GSIMouseListener, GSIScrollListener {

	private static final float SCROLL_MULTIPLIER = 20.0f;
	
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

	public GSScrollPanel() {
		this(null);
	}
	
	public GSScrollPanel(GSPanel content) {
		contentViewport = new GSViewport();
		columnHeaderViewport = new GSViewport();
		rowHeaderViewport = new GSViewport();

		verticalScrollBarPolicy = GSEScrollBarPolicy.SCROLLBAR_AS_NEEDED;
		horizontalScrollBarPolicy = GSEScrollBarPolicy.SCROLLBAR_AS_NEEDED;

		setVerticalScrollBar(new GSScrollBar());
		setHorizontalScrollBar(new GSScrollBar());

		setContent(content);
		setBottomRightCorner(new GSScrollPanelCorner());

		setLayoutManager(new GSScrollPanelLayoutManager());
	
		setFocusable(false);
		addMouseEventListener(this);
	}

	@Override
	protected void validate() {
		super.validate();

		// Viewport size might have changed, update scroll bars.
		updateScrollBarParams();
	}
	
	/* Visible for GSScrollPanelLayoutManager */
	GSViewport getContentViewport() {
		return contentViewport;
	}
	
	public GSPanel getContent() {
		return contentViewport.getContent();
	}

	public void setContent(GSPanel content) {
		contentViewport.setContent(content);

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
		columnHeaderViewport.setContent(columnHeader);
		
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
		rowHeaderViewport.setContent(rowHeader);

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
			verticalScrollBar.removeScrollListener(this);
		}
		
		verticalScrollBar = scrollBar;
		verticalScrollBar.addScrollListener(this);
		
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
			horizontalScrollBar.removeScrollListener(this);
		}
		
		horizontalScrollBar = scrollBar;
		horizontalScrollBar.addScrollListener(this);
		
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
			float newScrollOffsetX = horizontalScrollBar.getScrollOffset();
			float newScrollOffsetY = verticalScrollBar.getScrollOffset();

			// Shift will flip the xScroll and yScroll. This makes it
			// possible to scroll horizontally without any xScroll.
			if (Screen.hasShiftDown()) {
				newScrollOffsetX -= event.getScrollY() * SCROLL_MULTIPLIER;
				newScrollOffsetY -= event.getScrollX() * SCROLL_MULTIPLIER;
			} else {
				newScrollOffsetX -= event.getScrollX() * SCROLL_MULTIPLIER;
				newScrollOffsetY -= event.getScrollY() * SCROLL_MULTIPLIER;
			}
			
			if (!horizontalScrollBar.isAdded())
				newScrollOffsetX = 0.0f;
			if (!verticalScrollBar.isAdded())
				newScrollOffsetY = 0.0f;

			int mx = event.getX();
			int my = event.getY();
			
			if (!columnHeaderViewport.isEmpty() && columnHeaderViewport.isInBounds(mx, my)) {
				// The user is hovering over the column header. Ignore
				// scrollY to make scrolling more intuitive.
				horizontalScrollBar.setScrollOffset(newScrollOffsetX);
			} else if (!rowHeaderViewport.isEmpty() && rowHeaderViewport.isInBounds(mx, my)) {
				// Similarly, ignore scrollX when hovering the row header.
				verticalScrollBar.setScrollOffset(newScrollOffsetY);
			} else {
				horizontalScrollBar.setScrollOffset(newScrollOffsetX);
				verticalScrollBar.setScrollOffset(newScrollOffsetY);
			}
		}
	}

	@Override
	public void scrollChanged(float ignore) {
		// Called from both the vertical and horizontal scroll bar,
		// so we have to ignore the newScrollOffset from parameters.
		int offsetX = Math.round(-horizontalScrollBar.getScrollOffset());
		int offsetY = Math.round(-verticalScrollBar.getScrollOffset());
		// Update the offsets of the viewports.
		contentViewport.setOffset(offsetX, offsetY);
		columnHeaderViewport.setOffset(offsetX, 0);
		rowHeaderViewport.setOffset(0, offsetY);
	}
	
	private void updateScrollBarParams() {
		float maxScrollOffsetX = 0.0f;
		float maxScrollOffsetY = 0.0f;

		GSPanel content = getContent();
		if (content != null) {
			maxScrollOffsetX = content.getWidth();
			maxScrollOffsetY = content.getHeight();
		}
		GSPanel columnHeader = getColumnHeader();
		if (columnHeader != null && columnHeader.getWidth() > maxScrollOffsetX)
			maxScrollOffsetX = columnHeader.getWidth();
		GSPanel rowHeader = getRowHeader();
		if (rowHeader != null && rowHeader.getHeight() > maxScrollOffsetY)
			maxScrollOffsetY = rowHeader.getHeight();
		
		// Subtract the already visible viewport
		maxScrollOffsetX -= contentViewport.getWidth();
		maxScrollOffsetY -= contentViewport.getHeight();
		
		if (maxScrollOffsetX < 0.0f)
			maxScrollOffsetX = 0.0f;
		if (maxScrollOffsetY < 0.0f)
			maxScrollOffsetY = 0.0f;
		
		// Update minimum, maximum and view parameters of the scroll bar
		if (verticalScrollBar != null) {
			verticalScrollBar.setMinScrollOffset(0.0f);
			verticalScrollBar.setMaxScrollOffset(maxScrollOffsetY);
			verticalScrollBar.setViewSize(contentViewport.getHeight());
			verticalScrollBar.setVertical(true);
		}

		if (horizontalScrollBar != null) {
			horizontalScrollBar.setMinScrollOffset(0.0f);
			horizontalScrollBar.setMaxScrollOffset(maxScrollOffsetX);
			horizontalScrollBar.setViewSize(contentViewport.getWidth());
			horizontalScrollBar.setVertical(false);
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
}
