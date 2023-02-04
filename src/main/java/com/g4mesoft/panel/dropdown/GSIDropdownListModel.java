package com.g4mesoft.panel.dropdown;

import java.util.Collection;

public interface GSIDropdownListModel<T> {

	public void addListener(GSIDropdownListModelListener listener);

	public void removeListener(GSIDropdownListModelListener listener);
	
	public T getItem(int index);
	
	public void addItem(T item);

	public void addItem(int index, T item);
	
	public void addAll(Collection<T> items);

	public void addAll(int index, Collection<T> items);
	
	public void removeItem(int index);

	public void removeItem(T item);

	public int getIndex(T item);
	
	public int getCount();
	
	public void clear();
	
}
