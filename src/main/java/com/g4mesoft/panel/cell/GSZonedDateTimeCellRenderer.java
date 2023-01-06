package com.g4mesoft.panel.cell;

import java.time.ZonedDateTime;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.table.GSTablePanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSZonedDateTimeCellRenderer implements GSICellRenderer<ZonedDateTime> {

	public static final GSZonedDateTimeCellRenderer INSTANCE = new GSZonedDateTimeCellRenderer();
	
	private GSZonedDateTimeCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, ZonedDateTime value, GSRectangle bounds, GSTablePanel table) {
		GSStringCellRenderer.INSTANCE.render(renderer, GSPanelUtil.formatZonedDateTime(value), bounds, table);
	}
	
	@Override
	public GSDimension getMinimumSize(ZonedDateTime value) {
		return GSStringCellRenderer.INSTANCE.getMinimumSize(GSPanelUtil.formatZonedDateTime(value));
	}
}
