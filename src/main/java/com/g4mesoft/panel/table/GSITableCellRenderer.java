package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.renderer.GSIRenderer2D;

public interface GSITableCellRenderer<T> {

	public void render(GSIRenderer2D renderer, T value, GSRectangle bounds, GSTablePanel table);
	
	default public GSDimension getMinimumSize(T value) {
		return getPreferredSize(value);
	}

	public GSDimension getPreferredSize(T value);
	
}
