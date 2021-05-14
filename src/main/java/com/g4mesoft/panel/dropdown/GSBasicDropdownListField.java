package com.g4mesoft.panel.dropdown;

import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.field.GSTextField;

public class GSBasicDropdownListField<T> extends GSTextField implements GSIDropdownListField<T> {

	private T highlightedItem;
	
	@Override
	public GSPanel getFieldPanel() {
		return this;
	}

	@Override
	public GSIDropdownListFilter<T> getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHighlightedItem(T item) {
		highlightedItem = item;
	}

	@Override
	public T getHighlightedItem() {
		return highlightedItem;
	}
	
	@Override
	public void addActionListener(GSIActionListener listener) {
		super.addActionListener(listener);
	}

	@Override
	public void removeActionListener(GSIActionListener listener) {
		super.removeActionListener(listener);
	}
}
