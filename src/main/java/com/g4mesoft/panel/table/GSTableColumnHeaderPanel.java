package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSRectangle;
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
		
		renderBackground(renderer, clipBounds);
		renderHeaders(renderer, clipBounds);
	}
	
	private void renderBackground(GSIRenderer2D renderer, GSRectangle clipBounds) {
		if (table.getBackgroundColor() != 0) {
			int backgroundColor = GSColorUtil.darker(table.getBackgroundColor());
			renderer.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height, backgroundColor);
		}
	}
	
	private void renderHeaders(GSIRenderer2D renderer, GSRectangle clipBounds) {
		GSITableModel model = table.getModel();
		GSRectangle bounds = new GSRectangle();
		bounds.x = 0;
		bounds.y = clipBounds.y;
		bounds.height = clipBounds.height;
		for (int c = 0; c < model.getColumnCount() && bounds.x < clipBounds.x + clipBounds.width; c++) {
			GSITableColumn column = model.getColumn(c);
			bounds.width = column.getWidth();
			if (bounds.x + bounds.width >= clipBounds.x)
				renderHeader(renderer, column.getHeaderValue(), bounds);
			bounds.x += bounds.width;
		}
	}
	
	private <T> void renderHeader(GSIRenderer2D renderer, T value, GSRectangle bounds) {
		GSITableCellRenderer<T> cellRenderer = table.getCellRenderer(value);
		cellRenderer.render(renderer, value, bounds, table);
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
