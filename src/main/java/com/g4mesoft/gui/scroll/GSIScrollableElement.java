package com.g4mesoft.gui.scroll;

import com.g4mesoft.gui.GSIElement;

public interface GSIScrollableElement extends GSIElement, GSIScrollableViewport {

	default public int getScrollOffset() {
		if (getParent() != null) {
			GSIElement panel = getParent().getParent();
			if (panel instanceof GSScrollablePanel)
				return ((GSScrollablePanel)panel).getScrollOffset();
		}
		
		return 0;
	}
}
