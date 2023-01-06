package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.cell.GSICellRenderer;
import com.g4mesoft.panel.scroll.GSIScrollable;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

public class GSTableRowHeaderPanel extends GSPanel implements GSIScrollable {

	private final GSTablePanel table;

	public GSTableRowHeaderPanel(GSTablePanel table) {
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
		bounds.x = clipBounds.x;
		bounds.width = clipBounds.width;
		bounds.y = 0;
		for (int r = 0; r < model.getRowCount() && bounds.y < clipBounds.y + clipBounds.height; r++) {
			GSITableRow row = model.getRow(r);
			bounds.height = row.getHeight();
			if (bounds.y + bounds.height >= clipBounds.y)
				renderHeader(renderer, row.getHeaderValue(), bounds);
			bounds.y += bounds.height;
		}
	}
	
	private <T> void renderHeader(GSIRenderer2D renderer, T value, GSRectangle bounds) {
		GSICellRenderer<T> cellRenderer = table.getCellRenderer(value);
		cellRenderer.render(renderer, value, bounds, table);
	}
	
	@Override
	protected GSDimension calculateMinimumSize() {
		return GSTableLayoutManager.getRowHeaderSize(table, false, false);
	}

	@Override
	protected GSDimension calculatePreferredSize() {
		return GSTableLayoutManager.getRowHeaderSize(table, true, false);
	}

	@Override
	public GSDimension getMinimumScrollableSize() {
		return GSTableLayoutManager.getRowHeaderSize(table, false, true);
	}

	@Override
	public GSDimension getPreferredScrollableSize() {
		return GSTableLayoutManager.getRowHeaderSize(table, true, true);
	}

	@Override
	public boolean isScrollableHeightFilled() {
		return true;
	}
}
