package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.cell.GSCellContext;
import com.g4mesoft.panel.scroll.GSIScrollable;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

public class GSTableColumnHeaderPanel extends GSPanel implements GSIScrollable {

	private final GSTablePanel table;

	public GSTableColumnHeaderPanel(GSTablePanel table) {
		this.table = table;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);
		
		GSRectangle clipBounds = renderer.getClipBounds()
				.intersection(0, 0, width, height);
		
		GSCellContext context = table.createCellContext();
		context.backgroundColor = GSColorUtil.darker(context.backgroundColor);
		
		drawBackground(renderer, clipBounds, context);
		drawHeaders(renderer, clipBounds, context);
	}
	
	private void drawBackground(GSIRenderer2D renderer, GSRectangle clipBounds, GSCellContext context) {
		if (GSColorUtil.unpackA(context.backgroundColor) != 0x00)
			renderer.fillRect(clipBounds.x, clipBounds.y, clipBounds.width,
					clipBounds.height, context.backgroundColor);
	}
	
	private void drawHeaders(GSIRenderer2D renderer, GSRectangle clipBounds, GSCellContext context) {
		GSITableModel model = table.getModel();
		context.bounds.x = table.getVerticalBorderWidth();
		context.bounds.y = clipBounds.y;
		context.bounds.height = clipBounds.height;
		for (int c = 0; c < model.getColumnCount() && context.bounds.x < clipBounds.x + clipBounds.width; c++) {
			GSITableColumn column = model.getColumn(c);
			context.bounds.width = column.getWidth();
			if (context.bounds.x + context.bounds.width >= clipBounds.x)
				renderHeader(renderer, column.getHeaderValue(), context);
			context.bounds.x += context.bounds.width + table.getVerticalBorderWidth();
		}
	}
	
	private <T> void renderHeader(GSIRenderer2D renderer, T value, GSCellContext context) {
		table.getCellRenderer(value).render(renderer, value, context);
	}
	
	@Override
	protected GSDimension calculateMinimumSize() {
		return GSTableLayoutManager.getColumnHeaderSize(table, false, false);
	}

	@Override
	protected GSDimension calculatePreferredSize() {
		return GSTableLayoutManager.getColumnHeaderSize(table, true, false);
	}

	@Override
	public GSDimension getMinimumScrollableSize() {
		return GSTableLayoutManager.getColumnHeaderSize(table, false, true);
	}

	@Override
	public GSDimension getPreferredScrollableSize() {
		return GSTableLayoutManager.getColumnHeaderSize(table, true, true);
	}
	
	@Override
	public boolean isScrollableWidthFilled() {
		return true;
	}
}
