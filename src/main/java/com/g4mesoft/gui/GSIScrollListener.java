package com.g4mesoft.gui;

public interface GSIScrollListener {

	default public void preScrollChanged(double newScroll) {
	}
	
	public void scrollChanged(double newScroll);
	
}
