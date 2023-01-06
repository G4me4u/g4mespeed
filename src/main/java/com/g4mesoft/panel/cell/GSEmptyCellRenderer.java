package com.g4mesoft.panel.cell;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.table.GSTablePanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSEmptyCellRenderer implements GSICellRenderer<Object> {

	public static final GSEmptyCellRenderer INSTANCE = new GSEmptyCellRenderer();
	
	private GSEmptyCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, Object value, GSRectangle bounds, GSTablePanel table) {
		// Do nothing
	}
	
	@Override
	public GSDimension getMinimumSize(Object value) {
		return GSDimension.ZERO;
	}

	public static <T> GSICellRenderer<T> getInstance() {
		@SuppressWarnings("unchecked")
		GSICellRenderer<T> instance = (GSICellRenderer<T>)INSTANCE;
		return instance;
	}
}
