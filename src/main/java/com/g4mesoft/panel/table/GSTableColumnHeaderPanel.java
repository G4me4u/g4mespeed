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
		
		drawBackground(renderer, clipBounds);
		drawHeaders(renderer, clipBounds);
	}
	
	private GSCellContext initCellContext(GSCellContext context, int columnIndex, GSRectangle bounds) {
		context = table.initCellContext(context, columnIndex, GSTablePanel.INVALID_HEADER_INDEX, bounds);
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
		if (table.getColumnSelectionPolicy() != GSEHeaderSelectionPolicy.DISABLED &&
				table.hasSelection()) {
			GSRectangle bounds = new GSRectangle();
			bounds.y = 0;
			bounds.height = height;
			table.computeColumnSelectionBounds(bounds);
			int selectionColor = GSColorUtil.darker(table.getSelectionBackgroundColor());
			renderer.fillRect(bounds.x, bounds.y, bounds.width, bounds.height,
					selectionColor);
		}
	}
	
	private void drawHeaders(GSIRenderer2D renderer, GSRectangle clipBounds) {
		GSITableModel model = table.getModel();
		GSCellContext context = new GSCellContext();
		GSRectangle bounds = new GSRectangle();
		bounds.x = table.getVerticalBorderWidth();
		bounds.y = clipBounds.y;
		bounds.height = clipBounds.height;
		for (int c = 0; c < model.getColumnCount() && bounds.x < clipBounds.x + clipBounds.width; c++) {
			GSITableColumn column = model.getColumn(c);
			bounds.width = column.getWidth();
			if (bounds.x + bounds.width >= clipBounds.x) {
				initCellContext(context, c, bounds);
				renderHeader(renderer, column.getHeaderValue(), context);
			}
			bounds.x += bounds.width + table.getVerticalBorderWidth();
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
