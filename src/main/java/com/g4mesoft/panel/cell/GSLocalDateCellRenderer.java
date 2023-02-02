package com.g4mesoft.panel.cell;

import java.time.LocalDate;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.renderer.GSIRenderer2D;
public final class GSLocalDateCellRenderer implements GSICellRenderer<LocalDate> {

	public static final GSLocalDateCellRenderer INSTANCE = new GSLocalDateCellRenderer();
	
	private GSLocalDateCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, LocalDate value, GSCellContext context) {
		GSStringCellRenderer.INSTANCE.render(renderer, GSPanelUtil.formatLocalDate(value), context);
	}
	
	@Override
	public GSDimension getMinimumSize(LocalDate value) {
		return GSStringCellRenderer.INSTANCE.getMinimumSize(GSPanelUtil.formatLocalDate(value));
	}
}
