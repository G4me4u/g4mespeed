package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.LiteralText;

public final class GSStringTableCellRenderer implements GSITableCellRenderer<String> {

	public static final GSStringTableCellRenderer INSTANCE = new GSStringTableCellRenderer();
	
	private GSStringTableCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, String value, GSRectangle bounds, GSTablePanel table) {
		GSTextTableCellRenderer.INSTANCE.render(renderer, new LiteralText(value), bounds, table);
	}
	
	@Override
	public GSDimension getMinimumSize(String value) {
		return GSTextTableCellRenderer.INSTANCE.getMinimumSize(new LiteralText(value));
	}
}
