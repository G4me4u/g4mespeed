package com.g4mesoft.gui;

public interface GSILayoutManager {

	public GSDimension getMinimumSize(GSParentPanel parent);

	public GSDimension getPreferredSize(GSParentPanel parent);
	
	public void layoutChildren(GSParentPanel parent);
	
}
