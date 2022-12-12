package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.Text;

public final class GSTextTableCellRenderer implements GSITableCellRenderer<Text> {

	public static final GSTextTableCellRenderer INSTANCE = new GSTextTableCellRenderer();
	
	private GSTextTableCellRenderer() {
	}
	
	@Override
	public void render(GSIRenderer2D renderer, Text value, GSRectangle bounds, GSTablePanel table) {
		GSPanelUtil.drawLabel(renderer, null, 0, value, table.getTextColor(), true, null, GSETextAlignment.CENTER, bounds);
	}
	
	@Override
	public GSDimension getMinimumSize(Text value) {
		return GSPanelUtil.labelPreferredSize(null, value, 0);
	}
}
