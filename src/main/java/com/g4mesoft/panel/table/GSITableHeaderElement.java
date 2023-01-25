package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;

public interface GSITableHeaderElement {

	public Object getHeaderValue();

	public void setHeaderValue(Object value);
	
	public GSDimension getMinimumSize();
	
	public GSITableHeaderElement setMinimumSize(GSDimension minimumSize);

	public GSITableHeaderElement setMinimumWidth(int width);
	
	public GSITableHeaderElement setMinimumHeight(int height);
	
	public GSDimension getMaximumSize();
	
	public GSITableHeaderElement setMaximumSize(GSDimension maximumSize);
	
	public GSITableHeaderElement setMaximumWidth(int width);
	
	public GSITableHeaderElement setMaximumHeight(int height);

}
