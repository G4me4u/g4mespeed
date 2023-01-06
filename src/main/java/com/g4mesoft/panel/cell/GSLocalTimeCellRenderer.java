package com.g4mesoft.panel.cell;

import java.time.LocalTime;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.table.GSTablePanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSLocalTimeCellRenderer implements GSICellRenderer<LocalTime> {

	public static final GSLocalTimeCellRenderer INSTANCE = new GSLocalTimeCellRenderer();
	
	private GSLocalTimeCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, LocalTime value, GSRectangle bounds, GSTablePanel table) {
		GSStringCellRenderer.INSTANCE.render(renderer, GSPanelUtil.formatLocalTime(value), bounds, table);
	}
	
	@Override
	public GSDimension getMinimumSize(LocalTime value) {
		return GSStringCellRenderer.INSTANCE.getMinimumSize(GSPanelUtil.formatLocalTime(value));
	}
}
