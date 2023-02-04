package com.g4mesoft.panel.dropdown;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GSBasicDropdownListModel<T> implements GSIDropdownListModel<T> {

	private final List<T> items;
	
	private final List<GSIDropdownListModelListener> listeners;

	public GSBasicDropdownListModel() {
		this(new ArrayList<>());
	}
	
	public GSBasicDropdownListModel(T[] items) {
		this(new ArrayList<>(items.length));
		
		for (T item : items)
			this.items.add(item);
	}

	public GSBasicDropdownListModel(Collection<T> items) {
		this(new ArrayList<>(items));
	}

	private GSBasicDropdownListModel(List<T> items) {
		this.items = items;
		
		listeners = new ArrayList<>();
	}
	
	@Override
	public void addListener(GSIDropdownListModelListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		listeners.add(listener);
	}

	@Override
	public void removeListener(GSIDropdownListModelListener listener) {
		listeners.remove(listener);
	}
	
	private void dispatchIntervalAdded(int i0, int i1) {
		for (GSIDropdownListModelListener listener : listeners)
			listener.intervalAdded(i0, i1);
	}

	private void dispatchIntervalRemoved(int i0, int i1) {
		for (GSIDropdownListModelListener listener : listeners)
			listener.intervalRemoved(i0, i1);
	}
	
	@Override
	public T getItem(int index) {
		return items.get(index);
	}
	
	@Override
	public void addItem(T item) {
		if (item == null)
			throw new IllegalArgumentException("item is null!");
		int i0 = getCount();
		items.add(item);
		dispatchIntervalAdded(i0, i0 + 1);
	}

	@Override
	public void addItem(int index, T item) {
		if (item == null)
			throw new IllegalArgumentException("item is null!");
		items.add(index, item);
		dispatchIntervalAdded(index, index + 1);
	}
	
	@Override
	public void addAll(Collection<T> items) {
		int i0 = getCount();
		// Note: throws null-pointer exception on null list.
		this.items.addAll(items);
		dispatchIntervalAdded(i0, i0 + items.size());
	}

	@Override
	public void addAll(int index, Collection<T> items) {
		// Note: throws null-pointer exception on null list.
		this.items.addAll(index, items);
		dispatchIntervalAdded(index, index + items.size());
	}
	
	@Override
	public void removeItem(int index) {
		items.remove(index);
		dispatchIntervalRemoved(index, index + 1);
	}

	@Override
	public void removeItem(T item) {
		int index = getIndex(item);
		if (index != -1)
			removeItem(index);
	}

	@Override
	public int getIndex(T item) {
		return items.indexOf(item);
	}
	
	@Override
	public int getCount() {
		return items.size();
	}
	
	@Override
	public void clear() {
		int i1 = getCount();
		items.clear();
		dispatchIntervalRemoved(0, i1);
	}
}
