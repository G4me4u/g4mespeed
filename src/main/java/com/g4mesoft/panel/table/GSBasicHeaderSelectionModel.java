package com.g4mesoft.panel.table;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.util.GSMathUtil;

public class GSBasicHeaderSelectionModel implements GSIHeaderSelectionModel {

	private GSEHeaderSelectionPolicy policy;
	private int intervalMin;
	private int intervalMax;
	
	private int anchor;
	
	private final List<GSIHeaderSelectionListener> listeners;
	
	public GSBasicHeaderSelectionModel() {
		policy = GSEHeaderSelectionPolicy.SINGLE_INTERVAL_SELECTION;
		intervalMin = intervalMax = INVALID_SELECTION;
		anchor = INVALID_SELECTION;
		
		listeners = new ArrayList<>();
	}
	
	@Override
	public void addListener(GSIHeaderSelectionListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		listeners.add(listener);
	}

	@Override
	public void removeListener(GSIHeaderSelectionListener listener) {
		listeners.remove(listener);
	}

	public void dispatchSelectionChanged(int firstIndex, int lastIndex) {
		for (GSIHeaderSelectionListener listener : listeners)
			listener.selectionChanged(firstIndex, lastIndex);
	}
	
	@Override
	public void set(int index) {
		setInterval(index, index);
	}

	@Override
	public void setInterval(int i0, int i1) {
		if (policy == GSEHeaderSelectionPolicy.DISABLED) {
			// Do nothing
			return;
		}
		
		if (i0 == INVALID_SELECTION || i1 == INVALID_SELECTION) {
			// Invalidate selection
			i0 = i1 = INVALID_SELECTION;
		} else if (policy == GSEHeaderSelectionPolicy.SINGLE_SELECTION) {
			// Only select the second index.
			i0 = i1;
		}
		
		if (i0 != intervalMin || i1 != intervalMax)
			setIntervalImpl(i0, i1);
	}
	
	private void setIntervalImpl(int i0, int i1) {
		if (i0 < INVALID_SELECTION || i1 < INVALID_SELECTION)
			throw new IllegalArgumentException("Selection indices less than -1");
		int firstIndex = intervalMin, lastIndex = intervalMax;
		if (i0 == INVALID_SELECTION || i1 == INVALID_SELECTION) {
			intervalMin = intervalMax = INVALID_SELECTION;
		} else {
			intervalMin = Math.min(i0, i1);
			intervalMax = Math.max(i0, i1);
			firstIndex = Math.min(firstIndex, i0);
			lastIndex = Math.max(lastIndex, i1);
			// Ensure anchor is a selected cell
			anchor = GSMathUtil.clamp(anchor, intervalMin, intervalMax);
		}
		dispatchSelectionChanged(firstIndex, lastIndex);
	}

	@Override
	public void clear() {
		if (!isEmpty())
			setIntervalImpl(INVALID_SELECTION, INVALID_SELECTION);
	}

	@Override
	public int getIntervalMin() {
		return intervalMin;
	}

	@Override
	public int getIntervalMax() {
		return intervalMax;
	}

	@Override
	public boolean isSelected(int index) {
		return !isEmpty() && intervalMin <= index && index <= intervalMax;
	}

	@Override
	public boolean isEmpty() {
		// Note: either both or none are invalid.
		return intervalMin == INVALID_SELECTION;
	}

	@Override
	public void setSelectionPolicy(GSEHeaderSelectionPolicy policy) {
		if (policy == null)
			throw new IllegalArgumentException("policy is null!");
		if (policy != this.policy) {
			this.policy = policy;

			switch (policy) {
			case DISABLED:
				if (!isEmpty()) {
					// Invalidate selection
					setIntervalImpl(INVALID_SELECTION, INVALID_SELECTION);
				}
				break;
			case SINGLE_SELECTION:
				if (!isEmpty() && intervalMin != intervalMax) {
					// Select last index
					setIntervalImpl(intervalMax, intervalMax);
				}
				break;
			case SINGLE_INTERVAL_SELECTION:
				// Nothing to fix
				break;
			}
		}
	}

	@Override
	public GSEHeaderSelectionPolicy getSelectionPolicy() {
		return policy;
	}
	
	@Override
	public int getAnchor() {
		return anchor;
	}
	
	@Override
	public void setAnchor(int index) {
		anchor = GSMathUtil.clamp(index, intervalMin, intervalMax);
	}
}
