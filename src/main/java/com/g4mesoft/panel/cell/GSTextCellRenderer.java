package com.g4mesoft.panel.cell;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.table.GSTablePanel;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.Text;

public final class GSTextCellRenderer implements GSICellRenderer<Text> {

	public static final GSTextCellRenderer INSTANCE = new GSTextCellRenderer();
	
	private GSTextCellRenderer() {
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
