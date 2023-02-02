package com.g4mesoft.panel.cell;

import java.time.LocalTime;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSLocalTimeCellRenderer implements GSICellRenderer<LocalTime> {

	public static final GSLocalTimeCellRenderer INSTANCE = new GSLocalTimeCellRenderer();
	
	private GSLocalTimeCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, LocalTime value, GSCellContext context) {
		GSStringCellRenderer.INSTANCE.render(renderer, GSPanelUtil.formatLocalTime(value), context);
	}
	
	@Override
	public GSDimension getMinimumSize(LocalTime value) {
		return GSStringCellRenderer.INSTANCE.getMinimumSize(GSPanelUtil.formatLocalTime(value));
	}
}
