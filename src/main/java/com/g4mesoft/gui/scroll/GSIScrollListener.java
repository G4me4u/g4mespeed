package com.g4mesoft.gui.scroll;

public interface GSIScrollListener {

	default public void preScrollChanged(float newScroll) {
	}
	
	public void scrollChanged(float newScroll);
	
}
