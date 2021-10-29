package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSDimension;

public interface GSIScrollable {

	default public GSDimension getMinimumScrollableSize() {
		return null;
	}

	default public GSDimension getPreferredScrollableSize() {
		return null;
	}
	
	default public boolean isScrollableWidthFilled() {
		return false;
	}

	default public boolean isScrollableHeightFilled() {
		return false;
	}
	
	default public float getIncrementalScrollX(int sign) {
		return Float.NaN;
	}

	default public float getIncrementalScrollY(int sign) {
		return Float.NaN;
	}
}
