package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.event.GSILayoutEventListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSLayoutEvent;
import com.g4mesoft.panel.event.GSMouseEvent;

import net.minecraft.client.gui.screen.Screen;

public class GSScrollPanel extends GSParentPanel implements GSIMouseListener, GSIScrollListener {

	protected final GSViewport contentViewport;
	protected final GSViewport columnHeaderViewport;
	protected final GSViewport rowHeaderViewport;
	
	protected GSScrollBar verticalScrollBar;
	protected GSScrollBar horizontalScrollBar;
	
	protected GSPanel topLeftCorner;
	protected GSPanel topRightCorner;
	protected GSPanel bottomLeftCorner;
	protected GSPanel bottomRightCorner;
	
	protected GSEScrollBarPolicy verticalScrollBarPolicy;
	protected GSEScrollBarPolicy horizontalScrollBarPolicy;
	
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
			content.addLayoutEventListener(contentLayoutListener, -1);
		
		if (content instanceof GSIScrollable) {
			verticalScrollBar.setScrollable((GSIScrollable)content);
			horizontalScrollBar.setScrollable((GSIScrollable)content);
		} else {
			verticalScrollBar.setScrollable(null);
			horizontalScrollBar.setScrollable(null);
		}
		
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
			columnHeader.addLayoutEventListener(contentLayoutListener, -1);
		
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
			rowHeader.addLayoutEventListener(contentLayoutListener, -1);

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
			// Remove content from scroll bar
			verticalScrollBar.setScrollable(null);
		}
		
		verticalScrollBar = scrollBar;
		verticalScrollBar.addScrollListener(this);
		verticalScrollBar.setVertical(true);
		verticalScrollBar.setScrollable(getScrollableContent());
		
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
			// Remove content from scroll bar
			horizontalScrollBar.setScrollable(null);
		}
		
		horizontalScrollBar = scrollBar;
		horizontalScrollBar.addScrollListener(this);
		horizontalScrollBar.setVertical(false);
		horizontalScrollBar.setScrollable(getScrollableContent());
		
		invalidate();
	}
	
	private GSIScrollable getScrollableContent() {
		GSPanel content = getContent();
		if (content instanceof GSIScrollable)
			return (GSIScrollable)content;
		return null;
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
		if (!event.isConsumed() && !Screen.hasControlDown() && !Screen.hasAltDown()) {
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
		int offsetX = Math.round(horizontalScrollBar.getScroll());
		int offsetY = Math.round(verticalScrollBar.getScroll());
		// Update the offsets of the viewports.
		contentViewport.setOffset(offsetX, offsetY);
		columnHeaderViewport.setOffset(offsetX, 0);
		rowHeaderViewport.setOffset(0, offsetY);
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
	
	private class GSContentLayoutListener implements GSILayoutEventListener {
		
		@Override
		public void panelInvalidated(GSLayoutEvent event) {
			if (isValid() && !isValidating()) {
				// Content preferred size might have changed.
				invalidate();
			}
		}
	}
}
