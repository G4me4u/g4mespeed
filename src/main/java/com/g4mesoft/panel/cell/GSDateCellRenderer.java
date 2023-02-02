package com.g4mesoft.panel.cell;

import java.util.Date;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSDateCellRenderer implements GSICellRenderer<Date> {

	public static final GSDateCellRenderer INSTANCE = new GSDateCellRenderer();
	
	private GSDateCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, Date value, GSCellContext context) {
		GSStringCellRenderer.INSTANCE.render(renderer, GSPanelUtil.formatDate(value), context);
	}
	
	@Override
	public GSDimension getMinimumSize(Date value) {
		return GSStringCellRenderer.INSTANCE.getMinimumSize(GSPanelUtil.formatDate(value));
	}
}
