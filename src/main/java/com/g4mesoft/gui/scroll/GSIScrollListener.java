package com.g4mesoft.gui.scroll;

public interface GSIScrollListener {

	default public void preScrollChanged(double newScroll) {
	}
	
	public void scrollChanged(double newScroll);
	
}
