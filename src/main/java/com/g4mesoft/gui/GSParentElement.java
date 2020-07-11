package com.g4mesoft.gui;

import java.util.List;

public interface GSParentElement extends GSIElement {
	
	public void add(GSIElement element);
	
	public void remove(GSIElement element);
	
	public void removeAll();
	
	public List<GSIElement> getChildren();
	
}
