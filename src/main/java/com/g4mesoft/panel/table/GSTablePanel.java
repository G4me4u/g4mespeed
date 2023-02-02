package com.g4mesoft.panel.table;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSILayoutManager;
import com.g4mesoft.panel.GSIModelListener;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.cell.GSCellContext;
import com.g4mesoft.panel.cell.GSCellRendererRegistry;
import com.g4mesoft.panel.cell.GSICellRenderer;
import com.g4mesoft.panel.scroll.GSIScrollable;
import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

public class GSTablePanel extends GSParentPanel implements GSIScrollable,
                                                           GSITableModelListener {

	public static final int PREFERRED_COUNT_UNSPECIFIED = 0;
	
	private static final int DEFAULT_PREFERRED_COLUMN_COUNT = PREFERRED_COUNT_UNSPECIFIED;
	private static final int DEFAULT_PREFERRED_ROW_COUNT    = 10;
	private static final int DEFAULT_MINIMUM_COLUMN_WIDTH   = 30;

	private static final int DEFAULT_BACKGROUND_COLOR          = 0xFF202020;
	private static final int DEFAULT_DISABLED_BACKGROUND_COLOR = 0xFF0A0A0A;

	private static final int DEFAULT_TEXT_COLOR             = 0xFFE0E0E0;
	private static final int DEFAULT_DISABLED_TEXT_COLOR    = 0xFF707070;

	private static final int DEFAULT_BORDER_WIDTH           = 1;
	private static final int DEFAULT_BORDER_COLOR           = 0xFF161616;
	private static final int DEFAULT_DISABLED_BORDER_COLOR  = 0xFF060606;

	private GSITableModel model;
	
	private int preferredColumnCount;
	private int preferredRowCount;
	private int minimumColumnWidth;
	private int minimumRowHeight;
	
	private GSEHeaderResizePolicy columnHeaderResizePolicy;
	private GSEHeaderResizePolicy rowHeaderResizePolicy;
	
	private final GSCellRendererRegistry cellRendererRegistry;

	private int backgroundColor;
	private int disabledBackgroundColor;
	
	private int textColor;
	private int disabledTextColor;
	
	private int resizingColumnIndex;
	private int resizingRowIndex;
	
	private int verticalBorderWidth;
	private int horizontalBorderHeight;
	private int borderColor;
	private int disabledBorderColor;

	private GSTableColumnHeaderPanel columnHeader;
	private GSTableRowHeaderPanel rowHeader;
	
	private final List<GSIModelListener> modelListeners;
	
	public GSTablePanel(int columnCount, int rowCount) {
		this(new GSBasicTableModel(columnCount, rowCount));
	}
	
	public GSTablePanel(GSITableModel model) {
		super.setLayoutManager(new GSTableLayoutManager());
		
		preferredColumnCount = DEFAULT_PREFERRED_COLUMN_COUNT;
		preferredRowCount = DEFAULT_PREFERRED_ROW_COUNT;
		minimumColumnWidth = DEFAULT_MINIMUM_COLUMN_WIDTH;
		minimumRowHeight = GSPanelContext.getRenderer().getLineHeight();
		
		columnHeaderResizePolicy = GSEHeaderResizePolicy.RESIZE_SUBSEQUENT;
		rowHeaderResizePolicy = GSEHeaderResizePolicy.RESIZE_OFF;
		
		cellRendererRegistry = new GSCellRendererRegistry();
		
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		disabledBackgroundColor = DEFAULT_DISABLED_BACKGROUND_COLOR;
		
		textColor = DEFAULT_TEXT_COLOR;
		disabledTextColor = DEFAULT_DISABLED_TEXT_COLOR;
		
		resizingColumnIndex = resizingRowIndex = -1;

		verticalBorderWidth = horizontalBorderHeight = DEFAULT_BORDER_WIDTH;
		borderColor = DEFAULT_BORDER_COLOR;
		disabledBorderColor = DEFAULT_DISABLED_BORDER_COLOR;
		
		modelListeners = new ArrayList<>();
		
		setModel(model);
	}
	
	@Override
	public void add(GSPanel panel) {
		throw new UnsupportedOperationException("Adding panels only allowed internally");
	}

	@Override
	public void remove(GSPanel panel) {
		throw new UnsupportedOperationException("Removing panels only allowed internally");
	}
	
	@Override
	public void onAdded(GSPanel parent) {
		super.onAdded(parent);

		updateHeaderVisibility();
	}
	
	@Override
	public void onRemoved(GSPanel parent) {
		updateHeaderVisibility(true, true);
		
		super.onRemoved(parent);
	}

	private void updateHeaderVisibility() {
		updateHeaderVisibility(model.isColumnHeaderHidden(), model.isRowHeaderHidden());
	}
	
	private void updateHeaderVisibility(boolean columnHeaderHidden, boolean rowHeaderHidden) {
		GSScrollPanel scrollPanel = GSPanelUtil.getScrollPanel(this);
		if (scrollPanel instanceof GSScrollPanel) {
			boolean currentColumnHeaderHidden = scrollPanel.getColumnHeader() == null;
			boolean currentRowHeaderHidden = scrollPanel.getRowHeader() == null;
			
			if (columnHeaderHidden != currentColumnHeaderHidden) {
				if (currentColumnHeaderHidden) {
					scrollPanel.setColumnHeader(getColumnHeader());
					scrollPanel.setTopRightCorner(new GSTablePanelCorner(this));
					scrollPanel.setTopLeftCorner(new GSTablePanelCorner(this));
				} else if (columnHeader == scrollPanel.getColumnHeader()) {
					scrollPanel.setColumnHeader(null);
					scrollPanel.setTopRightCorner(null);
					scrollPanel.setTopLeftCorner(null);
				}
			}
			
			if (rowHeaderHidden != currentRowHeaderHidden) {
				if (currentRowHeaderHidden) {
					scrollPanel.setRowHeader(getRowHeader());
					scrollPanel.setBottomLeftCorner(new GSTablePanelCorner(this));
				} else if (rowHeader == scrollPanel.getRowHeader()) {
					scrollPanel.setRowHeader(null);
					scrollPanel.setBottomLeftCorner(null);
				}
			}
		}
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		GSCellContext context = createCellContext();
		
		drawBackground(renderer, context);
		
		int cx = verticalBorderWidth;
		for (int c = 0; c < model.getColumnCount() && cx < width; c++) {
			GSITableColumn column = model.getColumn(c);
			drawColumn(renderer, c, cx, column.getWidth(), context);
			cx += column.getWidth() + verticalBorderWidth;
		}
		
		drawBorder(renderer);
	}
	
	/* Visible for GSTableColumnHeaderPanel and GSTableRowHeaderPanel */
	GSCellContext createCellContext() {
		GSCellContext context = new GSCellContext();
		context.backgroundColor = isEnabled() ? backgroundColor : disabledBackgroundColor;
		context.textColor = isEnabled() ? textColor : disabledTextColor;
		// TODO: modify context text alignment.
		return context;
	}

	private void drawBackground(GSIRenderer2D renderer, GSCellContext context) {
		if (GSColorUtil.unpackA(context.backgroundColor) != 0x00)
			renderer.fillRect(0, 0, width, height, context.backgroundColor);
	}

	private void drawColumn(GSIRenderer2D renderer, int columnIndex, int columnX, int columnWidth, GSCellContext context) {
		context.bounds.x = columnX;
		context.bounds.width = columnWidth;
		context.bounds.y = horizontalBorderHeight;
		for (int r = 0; r < model.getRowCount() && context.bounds.y < height; r++) {
			GSITableRow row = model.getRow(r);
			context.bounds.height = row.getHeight();
			drawCell(renderer, columnIndex, r, context);
			context.bounds.y += context.bounds.height + horizontalBorderHeight;
		}
	}
	
	private void drawCell(GSIRenderer2D renderer, int columnIndex, int rowIndex, GSCellContext context) {
		drawCell(renderer, model.getCellValue(columnIndex, rowIndex), context);
	}

	private <T> void drawCell(GSIRenderer2D renderer, T cellValue, GSCellContext context) {
		getCellRenderer(cellValue).render(renderer, cellValue, context);
	}

	private void drawBorder(GSIRenderer2D renderer) {
		int color = isEnabled() ? borderColor : disabledBorderColor;
		if (GSColorUtil.unpackA(color) != 0x00) {
			if (verticalBorderWidth != 0) {
				// Compute total height of rows
				int h = horizontalBorderHeight * (model.getRowCount() + 1);
				for (int c = 0; c < model.getRowCount(); c++)
					h += model.getRow(c).getHeight();
				h = Math.min(height, h);
				// Draw border lines
				int x = 0;
				for (int c = 0; c <= model.getColumnCount() && x < width; c++) {
					renderer.fillRect(x, 0, verticalBorderWidth, h, color);
					x += verticalBorderWidth;
					// The last line does not have a following column.
					if (c != model.getColumnCount())
						x += model.getColumn(c).getWidth();
				}
			}
			if (horizontalBorderHeight != 0) {
				// Compute total width of columns
				int w = verticalBorderWidth * (model.getColumnCount() + 1);
				for (int c = 0; c < model.getColumnCount(); c++)
					w += model.getColumn(c).getWidth();
				w = Math.min(width, w);
				// Draw border lines
				int y = 0;
				for (int r = 0; r <= model.getRowCount() && y < height; r++) {
					renderer.fillRect(0, y, w, horizontalBorderHeight, color);
					y += horizontalBorderHeight;
					// The last line does not have a following row.
					if (r != model.getRowCount())
						y += model.getRow(r).getHeight();
				}
			}
		}
	}

	@Override
	public void setLayoutManager(GSILayoutManager layoutManager) {
		throw new UnsupportedOperationException();
	}
	
	public int getColumnIndexAtX(int x) {
		int index = -1, accX = 0;
		while (accX <= x) {
			index++;
			if (index >= model.getColumnCount())
				return -1;
			accX += model.getColumn(index).getWidth();
		}
		return index;
	}
	
	public int getColumnX(int index) {
		int accX = 0;
		// Compute first index that we do not consider
		int cmx = Math.min(index, model.getColumnCount());
		for (int c = 0; c < cmx; c++)
			accX += model.getColumn(c).getWidth();
		return accX;
	}

	public int getRowIndexAtY(int y) {
		int index = -1, accY = 0;
		while (accY <= y) {
			index++;
			if (index >= model.getRowCount())
				return -1;
			accY += model.getRow(index).getHeight();
		}
		return index;
	}
	
	public int getRowY(int index) {
		int accY = 0;
		// Compute first index that we do not consider
		int rmx = Math.min(index, model.getRowCount());
		for (int r = 0; r < rmx; r++)
			accY += model.getRow(r).getHeight();
		return accY;
	}
	
	public GSITableModel getModel() {
		return model;
	}
	
	public void setModel(GSITableModel model) {
		if (model == null)
			throw new IllegalArgumentException("model is null");
		
		if (this.model != null) {
			// Model is null when invoked from constructor
			this.model.removeListener(this);
			this.model.uninstall(this);
		}
		this.model = model;
		model.addListener(this);
		model.install(this);
		
		updateHeaderVisibility();
		invalidate();
		
		dispatchModelChangedEvent();
	}
	
	public void addModelListener(GSIModelListener listener) {
		modelListeners.add(listener);
	}

	public void removeModelListener(GSIModelListener listener) {
		modelListeners.remove(listener);
	}
	
	private void dispatchModelChangedEvent() {
		modelListeners.forEach(GSIModelListener::modelChanged);
	}
	
	public int getPreferredColumnCount() {
		return preferredColumnCount;
	}

	public void setPreferredColumnCount(int preferredColumnCount) {
		if (preferredColumnCount < 0)
			throw new IllegalArgumentException("preferredColumnCount must be non-negative!");
		this.preferredColumnCount = preferredColumnCount;
		invalidate();
	}

	public int getPreferredRowCount() {
		return preferredRowCount;
	}
	
	public void setPreferredRowCount(int preferredRowCount) {
		if (preferredRowCount < 0)
			throw new IllegalArgumentException("preferredRowCount must be non-negative!");
		this.preferredRowCount = preferredRowCount;
		invalidate();
	}
	
	public int getMinimumColumnWidth() {
		return minimumColumnWidth;
	}
	
	public void setMinimumColumnWidth(int minimumColumnWidth) {
		if (minimumColumnWidth < 0)
			throw new IllegalArgumentException("minimumColumnWidth must be non-negative!");
		this.minimumColumnWidth = minimumColumnWidth;
		invalidate();
	}

	public int getMinimumRowHeight() {
		return minimumRowHeight;
	}
	
	public void setMinimumRowHeight(int minimumRowHeight) {
		if (minimumRowHeight < 0)
			throw new IllegalArgumentException("minimumRowHeight must be non-negative!");
		this.minimumRowHeight = minimumRowHeight;
		invalidate();
	}
	
	public GSEHeaderResizePolicy getColumnHeaderResizePolicy() {
		return columnHeaderResizePolicy;
	}

	public void setColumnHeaderResizePolicy(GSEHeaderResizePolicy resizePolicy) {
		if (resizePolicy == null)
			throw new IllegalArgumentException("resizePolicy is null!");
		columnHeaderResizePolicy = resizePolicy;
		invalidate();
	}

	public GSEHeaderResizePolicy getRowHeaderResizePolicy() {
		return rowHeaderResizePolicy;
	}
	
	public void setRowHeaderResizePolicy(GSEHeaderResizePolicy resizePolicy) {
		if (resizePolicy == null)
			throw new IllegalArgumentException("resizePolicy is null!");
		rowHeaderResizePolicy = resizePolicy;
		invalidate();
	}

	public GSCellRendererRegistry getCellRendererRegistry() {
		return cellRendererRegistry;
	}
	
	public <T> GSICellRenderer<T> getCellRenderer(T value) {
		return cellRendererRegistry.getCellRenderer(value);
	}
	
	public <T> void setCellRenderer(Class<? extends T> valueClazz, GSICellRenderer<T> cellRenderer) {
		cellRendererRegistry.setCellRenderer(valueClazz, cellRenderer);
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int color) {
		backgroundColor = color;
	}

	public int getDisabledBackgroundColor() {
		return disabledBackgroundColor;
	}
	
	public void setDisabledBackgroundColor(int color) {
		disabledBackgroundColor = color;
	}
	
	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int color) {
		textColor = color;
	}

	public int getDisabledTextColor() {
		return disabledTextColor;
	}
	
	public void setDisabledTextColor(int color) {
		disabledTextColor = color;
	}

	public GSTableColumnHeaderPanel getColumnHeader() {
		if (columnHeader == null)
			columnHeader = new GSTableColumnHeaderPanel(this);
		return columnHeader;
	}
	
	public GSTableRowHeaderPanel getRowHeader() {
		if (rowHeader == null)
			rowHeader = new GSTableRowHeaderPanel(this);
		return rowHeader;
	}
	
	public int getResizingColumnIndex() {
		return resizingColumnIndex;
	}

	public int getResizingRowIndex() {
		return resizingRowIndex;
	}
	
	public int getVerticalBorderWidth() {
		return verticalBorderWidth;
	}

	public int getHorizontalBorderHeight() {
		return horizontalBorderHeight;
	}
	
	public void setBorderWidth(int verticalBorderWidth, int horizontalBorderHeight) {
		if (verticalBorderWidth < 0 || horizontalBorderHeight < 0)
			throw new IllegalArgumentException("borderWidth must be non-negative!");
		if (verticalBorderWidth != this.verticalBorderWidth ||
				horizontalBorderHeight != this.horizontalBorderHeight) {

			this.verticalBorderWidth = verticalBorderWidth;
			this.horizontalBorderHeight = horizontalBorderHeight;
			invalidate();
		}
	}
	
	public int getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(int color) {
		borderColor = color;
	}

	public int getDisabledBorderColor() {
		return disabledBorderColor;
	}
	
	public void setDisabledBorderColor(int color) {
		disabledBorderColor = color;
	}
	
	@Override
	public GSDimension getMinimumScrollableSize() {
		return GSTableLayoutManager.getTableSize(this, false, true);
	}

	@Override
	public GSDimension getPreferredScrollableSize() {
		return GSTableLayoutManager.getTableSize(this, true, true);
	}
	
	@Override
	public boolean isScrollableWidthFilled() {
		return true;
	}

	@Override
	public boolean isScrollableHeightFilled() {
		return true;
	}
	
	@Override
	public float getIncrementalScrollX(int sign) {
		int scrollX = GSPanelUtil.getScrollX(this);
		int columnIndex = getColumnIndexAtX(scrollX);
		if (columnIndex != -1) {
			// Translate by a single column depending on sign
			int delta = scrollX - getColumnX(columnIndex);
			if (sign > 0) {
				GSITableColumn column = model.getColumn(columnIndex);
				return column.getWidth() - delta;
			} else if (columnIndex > 0) {
				GSITableColumn prevColumn = model.getColumn(columnIndex - 1);
				return prevColumn.getWidth() + delta;
			}
		}
		return GSIScrollable.super.getIncrementalScrollX(sign);
	}

	@Override
	public float getIncrementalScrollY(int sign) {
		int scrollY = GSPanelUtil.getScrollY(this);
		int rowIndex = getRowIndexAtY(scrollY);
		if (rowIndex != -1) {
			// Translate by a single row depending on sign
			int delta = scrollY - getRowY(rowIndex);
			if (sign > 0) {
				GSITableRow row = model.getRow(rowIndex);
				return row.getHeight() - delta;
			} else if (rowIndex > 0) {
				GSITableRow prevRow = model.getRow(rowIndex - 1);
				return prevRow.getHeight() + delta;
			}
		}
		return GSIScrollable.super.getIncrementalScrollY(sign);
	}
	
	@Override
	public void cellValueChanged(int columnIndex, int rowIndex) {
		// TODO: figure out how to handle model change
	}

	@Override
	public void columnHeaderChanged(int columnIndex) {
		invalidate();
	}

	public void columnSizeChanged(int columnIndex) {
		if (!isValidating()) {
			// column#setWidth is invoked from the layout manager.
			invalidate();
		}
	}

	@Override
	public void rowHeaderChanged(int rowIndex) {
		invalidate();
	}

	@Override
	public void rowSizeChanged(int rowIndex) {
		if (!isValidating()) {
			// row#setHeight is invoked from the layout manager.
			invalidate();
		}
	}
	
	@Override
	public void headerVisibilityChanged() {
		updateHeaderVisibility();
	}
}
