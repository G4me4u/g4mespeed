package com.g4mesoft.panel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class GSPriorityEventListenerList<L> {

	private final List<GSEntry> listenerEntries;
	private boolean dirty;
	
	public GSPriorityEventListenerList() {
		listenerEntries = new ArrayList<>();
		dirty = false;
	}
	
	public void add(L listener, int priority) {
		listenerEntries.add(new GSEntry(listener, priority));
		dirty = true;
	}

	private int indexOf(Object o) {
		for (int i = 0; i < listenerEntries.size(); i++) {
			if (listenerEntries.get(i).listener == o)
				return i;
		}
		return -1;
	}
	
	public void remove(L listener) {
		int index = indexOf(listener);
		if (index != -1)
			listenerEntries.remove(index);
	}

	public boolean isEmpty() {
		return listenerEntries.isEmpty();
	}
	
	public Collection<L> asCollection() {
		if (dirty) {
			sort();
			dirty = false;
		}
		return new GSListenerCollection();
	}

	private void sort() {
		// Use standard sorting algorithm
		Collections.sort(listenerEntries);
	}
	
	private class GSEntry implements Comparable<GSEntry> {
		
		private final L listener;
		private final int priority;
		
		public GSEntry(L listener, int priority) {
			this.listener = listener;
			this.priority = priority;
		}
		
		@Override
		public int compareTo(GSEntry o) {
			// Descending order (opposite of integer natural ordering).
			return Integer.compare(o.priority, priority);
		}
	}
	
	private class GSListenerCollection implements Collection<L> {

		@Override
		public int size() {
			return listenerEntries.size();
		}

		@Override
		public boolean isEmpty() {
			return listenerEntries.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return indexOf(o) != -1;
		}

		@Override
		public Iterator<L> iterator() {
			return new GSListenerIterator();
		}

		@Override
	    public Object[] toArray() {
			int length = listenerEntries.size();
			Object[] arr = new Object[length];
			for (int i = 0; i < length; i++)
				arr[i] = listenerEntries.get(i).listener;
			return arr;
		}

	    @Override
	    public <T> T[] toArray(T[] arr) {
	    	int length = listenerEntries.size();
	        if (arr.length < length) {
	        	Class<? extends Object[]> arrType = arr.getClass();
	        	@SuppressWarnings("unchecked")
	            T[] newArr = (arrType == Object[].class) ? (T[])new Object[length] :
	            	(T[])Array.newInstance(arrType.getComponentType(), length);
	        	arr = newArr;
	        }
	        
			for (int i = 0; i < length; i++) {
				@SuppressWarnings("unchecked")
				T listener = (T)listenerEntries.get(i).listener;
				arr[i] = listener;
			}
	        if (arr.length > length)
	            arr[length] = null;

	        return arr;
	    }

		@Override
		public boolean add(L e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
	        for (Object e : c) {
	            if (!contains(e))
	                return false;
	        }
	        return true;
		}

		@Override
		public boolean addAll(Collection<? extends L> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		
        @Override
        public boolean removeIf(Predicate<? super L> filter) {
            throw new UnsupportedOperationException();
        }
	}
	
	private class GSListenerIterator implements Iterator<L> {

		private final Iterator<GSEntry> entryItr;
		
		public GSListenerIterator() {
			entryItr = listenerEntries.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return entryItr.hasNext();
		}

		@Override
		public L next() {
			return entryItr.next().listener;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
