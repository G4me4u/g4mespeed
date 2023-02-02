package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.cell.GSCellContext;
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
		
		GSCellContext context = table.createCellContext();
		context.backgroundColor = GSColorUtil.darker(context.backgroundColor);
		
		renderBackground(renderer, clipBounds, context);
		renderHeaders(renderer, clipBounds, context);
	}
	
	private void renderBackground(GSIRenderer2D renderer, GSRectangle clipBounds, GSCellContext context) {
		if (GSColorUtil.unpackA(context.backgroundColor) != 0x00)
			renderer.fillRect(clipBounds.x, clipBounds.y, clipBounds.width,
					clipBounds.height, context.backgroundColor);
	}
	
	private void renderHeaders(GSIRenderer2D renderer, GSRectangle clipBounds, GSCellContext context) {
		GSITableModel model = table.getModel();
		context.bounds.x = clipBounds.x;
		context.bounds.width = clipBounds.width;
		context.bounds.y = table.getHorizontalBorderHeight();
		for (int r = 0; r < model.getRowCount() && context.bounds.y < clipBounds.y + clipBounds.height; r++) {
			GSITableRow row = model.getRow(r);
			context.bounds.height = row.getHeight();
			if (context.bounds.y + context.bounds.height >= clipBounds.y)
				renderHeader(renderer, row.getHeaderValue(), context);
			context.bounds.y += context.bounds.height + table.getHorizontalBorderHeight();
		}
	}
	
	private <T> void renderHeader(GSIRenderer2D renderer, T value, GSCellContext context) {
		table.getCellRenderer(value).render(renderer, value, context);
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
