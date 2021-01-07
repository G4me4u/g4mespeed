package com.g4mesoft.panel.scroll;

public interface GSIScrollListener {

	default public void preScrollChanged(float newScroll) {
	}
	
	public void scrollChanged(float newScroll);
	
}
