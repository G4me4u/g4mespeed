package com.g4mesoft.panel.dropdown;

import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSPanel;

public interface GSIDropdownListField<T> {

	public GSPanel getFieldPanel();
	
	public GSIDropdownListFilter<T> getFilter();
	
	public void setHighlightedItem(T item);
	
	public T getHighlightedItem();
	
	public void addActionListener(GSIActionListener listener);
	
	public void removeActionListener(GSIActionListener listener);
	
}
