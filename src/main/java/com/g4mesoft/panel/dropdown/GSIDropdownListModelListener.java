package com.g4mesoft.panel.dropdown;

public interface GSIDropdownListModelListener {

	/**
	 * Invoked after an interval of elements is added, where {@code i0} is the index
	 * of the first element, and {@code i1} is the index of the first element after
	 * the interval. If there was previously elements starting at index {@code i0},
	 * the elements are moved to start at index {@code i1}.
	 * 
	 * @param i0 - the starting index of the interval (inclusive)
	 * @param i1 - the ending index of the interval (exclusive)
	 */
	public void intervalAdded(int i0, int i1);

	/**
	 * Invoked after an interval of elements is removed, where {@code i0} is the index
	 * of the first element, and {@code i1} is the index of the first element after
	 * the interval. If there was previously elements starting at index {@code i1}, the
	 * elements are moved to start at index {@code i0}.
	 * 
	 * @param i0 - the starting index of the interval (inclusive)
	 * @param i1 - the ending index of the interval (exclusive)
	 */
	public void intervalRemoved(int i0, int i1);

}
