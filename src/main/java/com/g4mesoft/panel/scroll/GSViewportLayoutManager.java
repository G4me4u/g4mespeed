package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSILayoutManager;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;

public class GSViewportLayoutManager implements GSILayoutManager {

	@Override
	public GSDimension getMinimumSize(GSParentPanel parent) {
		GSPanel content = ((GSViewport)parent).getContent();
		if (content == null)
			return GSDimension.ZERO;

		GSDimension minSize = null;
		if (content instanceof GSIScrollable)
			minSize = ((GSIScrollable)content).getMinimumScrollableSize();
		if (minSize == null)
			minSize = content.getProperty(GSPanel.MINIMUM_SIZE);
		
		return minSize;
	}

	@Override
	public GSDimension getPreferredSize(GSParentPanel parent) {
		GSPanel content = ((GSViewport)parent).getContent();
		if (content == null)
			return GSDimension.ZERO;

		GSDimension prefSize = null;
		if (content instanceof GSIScrollable)
			prefSize = ((GSIScrollable)content).getPreferredScrollableSize();
		if (prefSize == null)
			prefSize = content.getProperty(GSPanel.PREFERRED_SIZE);
		
		return prefSize;
	}

	@Override
	public void layoutChildren(GSParentPanel parent) {
		GSViewport viewport = (GSViewport)parent;
		GSPanel content = viewport.getContent();

		if (content != null) {
			int offsetX = viewport.getOffsetX();
			int offsetY = viewport.getOffsetY();

			GSDimension prefSize = getPreferredSize(parent);
			int prefW = prefSize.getWidth();
			int prefH = prefSize.getHeight();
			
			if (content instanceof GSIScrollable) {
				GSIScrollable scrollable = (GSIScrollable)content;
				if (scrollable.isScrollableWidthFilled()) {
					if (offsetX < 0) {
						// Use up the extra width to the left.
						prefW -= offsetX;
						offsetX = 0;
					}
					if (prefW < offsetX + parent.getWidth()) {
						// Use up the extra width to the right.
						prefW = offsetX + parent.getWidth();
					}
				}
				if (scrollable.isScrollableHeightFilled()) {
					if (offsetY < 0) {
						// Use up the extra height above.
						prefH -= offsetY;
						offsetY = 0;
					}
					if (prefH < offsetY + parent.getHeight()) {
						// Use up the extra height below.
						prefH = offsetY + parent.getHeight();
					}
				}
			}
			
			content.setBounds(offsetX, offsetY, prefW, prefH);
		}
	}
}
