package com.g4mesoft.panel.scroll;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSIChangeListener;

public abstract class GSAbstractScrollBarModel implements GSIScrollBarModel {

	private final List<GSIChangeListener> changeListeners;
	private final List<GSIScrollListener> scrollListeners;

	public GSAbstractScrollBarModel() {
		changeListeners = new ArrayList<>();
		scrollListeners = new ArrayList<>();
	}
	
	@Override
	public void addChangeListener(GSIChangeListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		changeListeners.add(listener);
	}

	@Override
	public void removeChangeListener(GSIChangeListener listener) {
		changeListeners.remove(listener);
	}

	protected void dispatchValueChanged() {
		changeListeners.forEach(GSIChangeListener::valueChanged);
	}

	@Override
	public void addScrollListener(GSIScrollListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		scrollListeners.add(listener);
	}
	
	@Override
	public void removeScrollListener(GSIScrollListener listener) {
		scrollListeners.remove(listener);
	}
	
	protected void dispatchScrollChanged(float newScroll) {
		for (GSIScrollListener listener : scrollListeners)
			listener.scrollChanged(newScroll);
	}
}
