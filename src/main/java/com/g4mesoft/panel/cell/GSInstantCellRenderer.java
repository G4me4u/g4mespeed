package com.g4mesoft.panel.cell;

import java.time.Instant;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.renderer.GSIRenderer2D;

public final class GSInstantCellRenderer implements GSICellRenderer<Instant> {

	public static final GSInstantCellRenderer INSTANCE = new GSInstantCellRenderer();
	
	private GSInstantCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, Instant value, GSCellContext context) {
		GSStringCellRenderer.INSTANCE.render(renderer, GSPanelUtil.formatInstant(value), context);
	}
	
	@Override
	public GSDimension getMinimumSize(Instant value) {
		return GSStringCellRenderer.INSTANCE.getMinimumSize(GSPanelUtil.formatInstant(value));
	}
}
