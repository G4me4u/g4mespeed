package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;

public interface GSITableHeaderElement {

	public Object getHeaderValue();

	public void setHeaderValue(Object value);
	
	public GSDimension getMinimumSize();
	
	public void setMinimumSize(GSDimension minimumSize);
	
	public GSDimension getMaximumSize();
	
	public void setMaximumSize(GSDimension preferredSize);

}
