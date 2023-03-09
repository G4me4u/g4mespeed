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
		
		drawBackground(renderer, clipBounds);
		drawHeaders(renderer, clipBounds);
	}
	
	private GSCellContext initCellContext(GSCellContext context, int rowIndex, GSRectangle bounds) {
		context = table.initCellContext(context, GSTablePanel.INVALID_HEADER_INDEX, rowIndex, bounds);
		context.backgroundColor = GSColorUtil.darker(context.backgroundColor);
		return context;
	}
	
	private void drawBackground(GSIRenderer2D renderer, GSRectangle clipBounds) {
		GSCellContext context = initCellContext(null, GSTablePanel.INVALID_HEADER_INDEX, null);
		if (GSColorUtil.unpackA(context.backgroundColor) != 0x00) {
			renderer.fillRect(clipBounds.x, clipBounds.y, clipBounds.width,
					clipBounds.height, context.backgroundColor);
		}
		// Check if we have a selection and draw its background
		if (table.getRowSelectionPolicy() != GSEHeaderSelectionPolicy.DISABLED &&
				table.hasSelection()) {
			GSRectangle bounds = new GSRectangle();
			bounds.x = 0;
			bounds.width = width;
			table.computeRowSelectionBounds(bounds);
			int selectionColor = GSColorUtil.darker(table.getSelectionBackgroundColor());
			renderer.fillRect(bounds.x, bounds.y, bounds.width, bounds.height,
					selectionColor);
		}
	}
	
	private void drawHeaders(GSIRenderer2D renderer, GSRectangle clipBounds) {
		GSITableModel model = table.getModel();
		GSCellContext context = new GSCellContext();
		GSRectangle bounds = new GSRectangle();
		bounds.x = clipBounds.x;
		bounds.width = clipBounds.width;
		bounds.y = table.getHorizontalBorderHeight();
		for (int r = 0; r < model.getRowCount() && bounds.y < clipBounds.y + clipBounds.height; r++) {
			GSITableRow row = model.getRow(r);
			bounds.height = row.getHeight();
			if (bounds.y + bounds.height >= clipBounds.y) {
				initCellContext(context, r, bounds);
				renderHeader(renderer, row.getHeaderValue(), context);
			}
			bounds.y += bounds.height + table.getHorizontalBorderHeight();
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
