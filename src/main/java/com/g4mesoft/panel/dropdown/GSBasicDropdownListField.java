package com.g4mesoft.panel.dropdown;

import com.g4mesoft.panel.GSIChangeListener;
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
	public void addChangeListener(GSIChangeListener listener) {
		super.addChangeListener(listener);
	}

	@Override
	public void removeChangeListener(GSIChangeListener listener) {
		super.removeChangeListener(listener);
	}
}
