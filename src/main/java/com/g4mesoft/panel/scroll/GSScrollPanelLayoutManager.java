package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSILayoutManager;
import com.g4mesoft.panel.GSILayoutProperty;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;

public class GSScrollPanelLayoutManager implements GSILayoutManager {

	@Override
	public GSDimension getMinimumInnerSize(GSParentPanel parent) {
		return computeSize(parent, GSPanel.MINIMUM_SIZE);
	}

	@Override
	public GSDimension getPreferredInnerSize(GSParentPanel parent) {
		return computeSize(parent, GSPanel.PREFERRED_SIZE);
	}
	
	private GSDimension computeSize(GSParentPanel parent, GSILayoutProperty<GSDimension> sizeProperty) {
		GSScrollPanel scrollPanel = (GSScrollPanel)parent;
		
		GSViewport contentViewport = scrollPanel.getContentViewport();
		GSDimension contentSize = contentViewport.getProperty(sizeProperty);
		long wl = contentSize.getWidth();
		long hl = contentSize.getHeight();
		
		GSViewport columnHeaderViewport = scrollPanel.getColumnHeaderViewport();
		if (!columnHeaderViewport.isEmpty()) {
			GSDimension columnHeaderSize = columnHeaderViewport.getProperty(sizeProperty);
			if (columnHeaderSize.getWidth() > wl)
				wl = columnHeaderSize.getWidth();
			hl += columnHeaderSize.getHeight();
		}

		GSViewport rowHeaderViewport = scrollPanel.getColumnHeaderViewport();
		if (!rowHeaderViewport.isEmpty()) {
			GSDimension rowHeaderSize = rowHeaderViewport.getProperty(sizeProperty);
			wl += rowHeaderSize.getWidth();
			if (rowHeaderSize.getHeight() > hl)
				hl = rowHeaderSize.getHeight();
		}
		
		if (scrollPanel.getVerticalScrollBarPolicy() == GSEScrollBarPolicy.SCROLLBAR_ALWAYS) {
			GSScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
			GSDimension vsbSize = verticalScrollBar.getProperty(sizeProperty);
			wl += vsbSize.getWidth();
			if (vsbSize.getHeight() > hl)
				hl = vsbSize.getHeight();
		}

		if (scrollPanel.getHorizontalScrollBarPolicy() == GSEScrollBarPolicy.SCROLLBAR_ALWAYS) {
			GSScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();
			GSDimension hsbSize = horizontalScrollBar.getProperty(sizeProperty);
			if (hsbSize.getWidth() > wl)
				wl = hsbSize.getWidth();
			hl += hsbSize.getHeight();
		}
		
		int w = (int)Math.min(wl, Integer.MAX_VALUE);
		int h = (int)Math.min(hl, Integer.MAX_VALUE);
		
		return new GSDimension(w, h);
	}

	@Override
	public void layoutChildren(GSParentPanel parent) {
		GSScrollPanel scrollPanel = (GSScrollPanel)parent;
		
		GSViewport contentViewport = scrollPanel.getContentViewport();
		GSViewport columnHeaderViewport = scrollPanel.getColumnHeaderViewport();
		GSViewport rowHeaderViewport = scrollPanel.getColumnHeaderViewport();

		GSPanel content = contentViewport.getContent();
		
		boolean sWidthFixed = false;
		boolean sHeightFixed = false;
		if (content instanceof GSIScrollable) {
			sWidthFixed  = ((GSIScrollable)content).isScrollableWidthFixed();
			sHeightFixed = ((GSIScrollable)content).isScrollableHeightFixed();
		}
		
		int availW = parent.getInnerWidth();
		int availH = parent.getInnerHeight();

		boolean chPresent = !columnHeaderViewport.isEmpty();
		boolean rhPresent = !rowHeaderViewport.isEmpty();
		
		int chh = 0;
		if (chPresent) {
			GSDimension size = columnHeaderViewport.getProperty(GSPanel.PREFERRED_SIZE);
			chh = Math.min(availH, size.getHeight());
			availH -= chh;
		}
		
		int rhw = 0;
		if (rhPresent) {
			GSDimension size = rowHeaderViewport.getProperty(GSPanel.PREFERRED_SIZE);
			rhw = Math.min(availW, size.getWidth());
			availW -= rhw;
		}

		int contentPrefW = contentViewport.getProperty(GSPanel.PREFERRED_WIDTH);
		int contentPrefH = contentViewport.getProperty(GSPanel.PREFERRED_HEIGHT);
		
		GSScrollBar verticalScrollBar = scrollPanel.getVerticalScrollBar();
		GSScrollBar horizontalScrollBar = scrollPanel.getHorizontalScrollBar();

		GSDimension vsbPrefSize = verticalScrollBar.getProperty(GSPanel.PREFERRED_SIZE);
		GSDimension hsbPrefSize = horizontalScrollBar.getProperty(GSPanel.PREFERRED_SIZE);

		boolean vsbNeeded;
		switch (scrollPanel.getVerticalScrollBarPolicy()) {
		case SCROLLBAR_ALWAYS:
			vsbNeeded = true;
			break;
		case SCROLLBAR_AS_NEEDED:
		default:
			vsbNeeded = (!sHeightFixed && availH < contentPrefH);
			break;
		case SCROLLBAR_NEVER:
			vsbNeeded = false;
			break;
		}

		if (vsbNeeded)
			availW -= vsbPrefSize.getWidth();
		
		boolean hsbNeeded;
		switch (scrollPanel.getHorizontalScrollBarPolicy()) {
		case SCROLLBAR_ALWAYS:
			hsbNeeded = true;
			break;
		case SCROLLBAR_AS_NEEDED:
		default:
			hsbNeeded = (!sWidthFixed && availW < contentPrefW);
			break;
		case SCROLLBAR_NEVER:
			hsbNeeded = true;
			break;
		}
		
		if (hsbNeeded) {
			availH -= hsbPrefSize.getHeight();

			if (!vsbNeeded && scrollPanel.getVerticalScrollBarPolicy() == GSEScrollBarPolicy.SCROLLBAR_AS_NEEDED) {
				// Check if we have to add the vertical scroll bar with
				// the updated height from the horizontal scroll bar.
				if (availH < contentPrefH) {
					vsbNeeded = true;
					availW -= vsbPrefSize.getWidth();
				}
			}
		}
		
		if (sWidthFixed)
			contentPrefW = availW;
		if (sHeightFixed)
			contentPrefH = availH;
		
		// Add and set bounds of the panels in the scroll panel.
		
		ensureAdded(parent, contentViewport);
		contentViewport.setOuterBounds(rhw, chh, availW, availH);
		
		if (chPresent) {
			ensureAdded(parent, columnHeaderViewport);
			columnHeaderViewport.setOuterBounds(rhw, 0, availW, chh);
		} else {
			ensureRemoved(parent, columnHeaderViewport);
		}
		
		if (rhPresent) {
			ensureAdded(parent, rowHeaderViewport);
			rowHeaderViewport.setOuterBounds(0, chh, rhw, availH);
		} else {
			ensureRemoved(parent, rowHeaderViewport);
		}
		
		if (vsbNeeded) {
			ensureAdded(parent, verticalScrollBar);
			verticalScrollBar.setOuterBounds(rhw + availW, chh, vsbPrefSize.getWidth(), availH);
		} else {
			ensureRemoved(parent, verticalScrollBar);
		}
		
		if (hsbNeeded) {
			ensureAdded(parent, horizontalScrollBar);
			horizontalScrollBar.setOuterBounds(rhw, chh + availH, availW, hsbPrefSize.getHeight());
		} else {
			ensureRemoved(parent, horizontalScrollBar);
		}
		
		// Add corners if they should be visible
		
		GSPanel topLeftCorner = scrollPanel.getTopLeftCorner();
		if (topLeftCorner != null) {
			if (chPresent && rhPresent) {
				ensureAdded(parent, topLeftCorner);
				topLeftCorner.setOuterBounds(0, 0, rhw, chh);
			} else {
				ensureRemoved(parent, topLeftCorner);
			}
		}

		GSPanel topRightCorner = scrollPanel.getTopRightCorner();
		if (topRightCorner != null) {
			if (chPresent && vsbNeeded) {
				ensureAdded(parent, topRightCorner);
				topRightCorner.setOuterBounds(rhw + availW, 0, vsbPrefSize.getWidth(), chh);
			} else {
				ensureRemoved(parent, topRightCorner);
			}
		}

		GSPanel bottomLeftCorner = scrollPanel.getBottomLeftCorner();
		if (bottomLeftCorner != null) {
			if (rhPresent && hsbNeeded) {
				ensureAdded(parent, bottomLeftCorner);
				bottomLeftCorner.setOuterBounds(0, chh + availH, rhw, hsbPrefSize.getHeight());
			} else {
				ensureRemoved(parent, bottomLeftCorner);
			}
		}

		GSPanel bottomRightCorner = scrollPanel.getBottomRightCorner();
		if (bottomRightCorner != null) {
			if (vsbNeeded && hsbNeeded) {
				ensureAdded(parent, bottomRightCorner);
				bottomRightCorner.setOuterBounds(rhw + availW, chh + availH, vsbPrefSize.getWidth(), hsbPrefSize.getHeight());
			} else {
				ensureRemoved(parent, bottomRightCorner);
			}
		}
	}
	
	private void ensureAdded(GSParentPanel parent, GSPanel panel) {
		if (!panel.isAdded())
			parent.add(panel);
	}

	private void ensureRemoved(GSParentPanel parent, GSPanel panel) {
		if (panel.isAdded())
			parent.remove(panel);
	}
}
