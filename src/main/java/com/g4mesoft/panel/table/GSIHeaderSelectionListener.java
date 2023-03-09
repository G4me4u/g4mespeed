package com.g4mesoft.panel.table;

public interface GSIHeaderSelectionListener {

	/**
	 * Invoked after the header selection interval has changed.
	 * 
	 * @param firstIndex - the index of the first value which might have
	 *                     changed selection.
	 * @param lastIndex - the index of the last value which might have
	 *                    changed selection.
	 */
	public void selectionChanged(int firstIndex, int lastIndex);
	
}
