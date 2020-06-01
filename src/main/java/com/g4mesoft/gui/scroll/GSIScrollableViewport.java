package com.g4mesoft.gui.scroll;

import com.g4mesoft.gui.GSIViewport;

public interface GSIScrollableViewport extends GSIViewport {

	public int getContentWidth();
	
	public int getContentHeight();
	
	default public int getContentViewWidth() {
		return getWidth();
	}

	default public int getContentViewHeight() {
		return getHeight();
	}
	
	default public double getIncrementalScrollX(int sign) {
		return Double.NaN;
	}

	default public double getIncrementalScrollY(int sign) {
		return Double.NaN;
	}
}
