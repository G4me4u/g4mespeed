package com.g4mesoft.panel.cell;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.table.GSTablePanel;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.LiteralText;

public final class GSStringCellRenderer implements GSICellRenderer<String> {

	public static final GSStringCellRenderer INSTANCE = new GSStringCellRenderer();
	
	private GSStringCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, String value, GSRectangle bounds, GSTablePanel table) {
		GSTextCellRenderer.INSTANCE.render(renderer, new LiteralText(value), bounds, table);
	}
	
	@Override
	public GSDimension getMinimumSize(String value) {
		return GSTextCellRenderer.INSTANCE.getMinimumSize(new LiteralText(value));
	}
}
