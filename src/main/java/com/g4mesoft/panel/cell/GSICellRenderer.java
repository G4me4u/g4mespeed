package com.g4mesoft.panel.cell;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.table.GSTablePanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public interface GSICellRenderer<T> {

	public void render(GSIRenderer2D renderer, T value, GSRectangle bounds, GSTablePanel table);
	
	public GSDimension getMinimumSize(T value);

	default public GSDimension getMaximumSize(T value) {
		return GSDimension.MAX_VALUE;
	}
}
