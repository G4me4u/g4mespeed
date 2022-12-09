package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSEmptyCellRenderer implements GSITableCellRenderer<Object> {

	public static final GSEmptyCellRenderer INSTANCE = new GSEmptyCellRenderer();
	
	private GSEmptyCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, Object value, GSRectangle bounds, GSTablePanel table) {
		// Do nothing
	}
	
	@Override
	public GSDimension getPreferredSize(Object value) {
		return GSDimension.ZERO;
	}
}
