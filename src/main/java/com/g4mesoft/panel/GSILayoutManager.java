package com.g4mesoft.panel;

public interface GSILayoutManager {

	public GSDimension getMinimumInnerSize(GSParentPanel parent);

	public GSDimension getPreferredInnerSize(GSParentPanel parent);
	
	public void layoutChildren(GSParentPanel parent);
	
}
