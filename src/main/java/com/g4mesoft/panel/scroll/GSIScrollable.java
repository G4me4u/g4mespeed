package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSIViewport;
import com.g4mesoft.panel.GSPanel;

public interface GSIScrollable extends GSIViewport {

	public int getContentWidth();
	
	public int getContentHeight();
	
	default public int getContentViewWidth() {
		return getWidth();
	}

	default public int getContentViewHeight() {
		return getHeight();
	}
	
	default public float getIncrementalScrollX(int sign) {
		return Float.NaN;
	}

	default public float getIncrementalScrollY(int sign) {
		return Float.NaN;
	}
	
	default public int getScrollOffset(GSPanel parent) {
		if (parent != null) {
			GSPanel scrollablePanel = parent.getParent();
			if (scrollablePanel instanceof GSScrollPanel)
				return ((GSScrollPanel)scrollablePanel).getScrollOffset();
		}
		
		return 0;
	}
}
