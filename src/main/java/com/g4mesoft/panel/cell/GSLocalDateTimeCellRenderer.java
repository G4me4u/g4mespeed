package com.g4mesoft.panel.cell;

import java.time.LocalDateTime;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.table.GSTablePanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSLocalDateTimeCellRenderer implements GSICellRenderer<LocalDateTime> {

	public static final GSLocalDateTimeCellRenderer INSTANCE = new GSLocalDateTimeCellRenderer();
	
	private GSLocalDateTimeCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, LocalDateTime value, GSRectangle bounds, GSTablePanel table) {
		GSStringCellRenderer.INSTANCE.render(renderer, GSPanelUtil.formatLocalDateTime(value), bounds, table);
	}
	
	@Override
	public GSDimension getMinimumSize(LocalDateTime value) {
		return GSStringCellRenderer.INSTANCE.getMinimumSize(GSPanelUtil.formatLocalDateTime(value));
	}
}
