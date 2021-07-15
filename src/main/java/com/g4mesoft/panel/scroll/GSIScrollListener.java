package com.g4mesoft.panel.scroll;

import com.g4mesoft.core.GSCoreOverride;

public interface GSIScrollListener {

	default public void preScrollChanged(float newScroll) {
	}
	
	@GSCoreOverride
	public void scrollChanged(float newScroll);
	
}
