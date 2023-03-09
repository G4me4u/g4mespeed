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
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.cell.GSCellContext;
import com.g4mesoft.panel.cell.GSCellRendererRegistry;
import com.g4mesoft.panel.cell.GSICellRenderer;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.panel.scroll.GSIScrollable;
import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

public class GSTablePanel extends GSParentPanel implements GSIMouseListener,
                                                           GSIScrollable,
                                                           GSITableModelListener {

	public static final int PREFERRED_COUNT_UNSPECIFIED = 0;
	
	private static final int DEFAULT_PREFERRED_COLUMN_COUNT = PREFERRED_COUNT_UNSPECIFIED;
	private static final int DEFAULT_PREFERRED_ROW_COUNT = 10;
	private static final int DEFAULT_MINIMUM_COLUMN_WIDTH = 30;

	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF202020;
	private static final int DEFAULT_SELECTION_BACKGROUND_COLOR = 0xFF094771;
	private static final int DEFAULT_DISABLED_BACKGROUND_COLOR = 0xFF0A0A0A;

	private static final int DEFAULT_TEXT_COLOR = 0xFFCCCCCC;
	private static final int DEFAULT_SELECTION_TEXT_COLOR = 0xFFF3F6F8;
	private static final int DEFAULT_DISABLED_TEXT_COLOR = 0xFF686869;

	private static final int DEFAULT_BORDER_WIDTH = 1;
	private static final int DEFAULT_BORDER_COLOR = 0xFF171717;
	private static final int DEFAULT_SELECTION_BORDER_COLOR = 0xFF06314F;
	private static final int DEFAULT_DISABLED_BORDER_COLOR = 0xFF060606;
	
	public static final int INVALID_HEADER_INDEX = -1;

	private GSITableModel model;
	
	private int preferredColumnCount;
	private int preferredRowCount;
	private int minimumColumnWidth;
	private int minimumRowHeight;
	
	private GSEHeaderResizePolicy columnHeaderResizePolicy;
	private GSEHeaderResizePolicy rowHeaderResizePolicy;
	
	private final GSCellRendererRegistry cellRendererRegistry;

	private int backgroundColor;
	private int selectionBackgroundColor;
	private int disabledBackgroundColor;
	
	private int textColor;
	private int selectionTextColor;
	private int disabledTextColor;
	
	private int resizingColumnIndex;
	private int resizingRowIndex;
	
	private int verticalBorderWidth;
	private int horizontalBorderHeight;
	private int borderColor;
	private int selectionBorderColor;
	private int disabledBorderColor;

	private GSTableColumnHeaderPanel columnHeader;
	private GSTableRowHeaderPanel rowHeader;
	
	private final GSIHeaderSelectionModel columnSelectionModel;
	private final GSIHeaderSelectionModel rowSelectionModel;
	
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
		selectionBackgroundColor = DEFAULT_SELECTION_BACKGROUND_COLOR;
		disabledBackgroundColor = DEFAULT_DISABLED_BACKGROUND_COLOR;
		
		textColor = DEFAULT_TEXT_COLOR;
		selectionTextColor = DEFAULT_SELECTION_TEXT_COLOR;
		disabledTextColor = DEFAULT_DISABLED_TEXT_COLOR;
		
		resizingColumnIndex = resizingRowIndex = INVALID_HEADER_INDEX;

		verticalBorderWidth = horizontalBorderHeight = DEFAULT_BORDER_WIDTH;
		borderColor = DEFAULT_BORDER_COLOR;
		selectionBorderColor = DEFAULT_SELECTION_BORDER_COLOR;
		disabledBorderColor = DEFAULT_DISABLED_BORDER_COLOR;
		
		columnSelectionModel = new GSBasicHeaderSelectionModel();
		rowSelectionModel = new GSBasicHeaderSelectionModel();
		
		modelListeners = new ArrayList<>();
		
		setModel(model);
		
		setColumnSelectionPolicy(GSEHeaderSelectionPolicy.DISABLED);
		setRowSelectionPolicy(GSEHeaderSelectionPolicy.SINGLE_INTERVAL_SELECTION);
		
		addMouseEventListener(this);
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

		GSRectangle clipBounds = renderer.getClipBounds()
				.intersection(0, 0, width, height);
		
		drawBackground(renderer, clipBounds);
		drawColumns(renderer, clipBounds);
		drawBorders(renderer, clipBounds);
	}
	
	/* Visible for GSTableColumnHeaderPanel and GSTableRowHeaderPanel */
	GSCellContext initCellContext(GSCellContext context, int columnIndex, int rowIndex) {
		return initCellContext(context, columnIndex, rowIndex, null);
	}

	/* Visible for GSTableColumnHeaderPanel and GSTableRowHeaderPanel */
	GSCellContext initCellContext(GSCellContext context, int columnIndex, int rowIndex, GSRectangle bounds) {
		if (context == null)
			context = new GSCellContext();
		if (isEnabled()) {
			// Check if cell is selected and update accordingly
			if (isCellSelected(columnIndex, rowIndex)) {
				context.backgroundColor = selectionBackgroundColor;
				context.textColor = selectionTextColor;
			} else {
				context.backgroundColor = backgroundColor;
				context.textColor = textColor;
			}
		} else {
			context.backgroundColor = disabledBackgroundColor;
			context.textColor = disabledTextColor;
		}
		// TODO: modify context text alignment based on row and column index.
		if (bounds == null) {
			if (columnIndex != INVALID_HEADER_INDEX && rowIndex != INVALID_HEADER_INDEX)
				getInnerCellBounds(columnIndex, rowIndex, context.bounds);
		} else {
			context.bounds.setBounds(bounds);
		}
		return context;
	}

	private void drawBackground(GSIRenderer2D renderer, GSRectangle clipBounds) {
		GSCellContext context = initCellContext(null, INVALID_HEADER_INDEX, INVALID_HEADER_INDEX);
		if (GSColorUtil.unpackA(context.backgroundColor) != 0x00) {
			renderer.fillRect(clipBounds.x, clipBounds.y, clipBounds.width,
					clipBounds.height, context.backgroundColor);
		}
		// Check if we have a selection and draw its background
		GSRectangle bounds = getSelectionBounds();
		if (bounds != null) {
			renderer.fillRect(bounds.x, bounds.y, bounds.width, bounds.height,
					selectionBackgroundColor);
		}
	}
	
	private void drawColumns(GSIRenderer2D renderer, GSRectangle clipBounds) {
		int cx = verticalBorderWidth;
		for (int c = 0; c < model.getColumnCount() && cx < clipBounds.x + clipBounds.width; c++) {
			int columnWidth = getColumnWidth(c);
			if (cx + columnWidth >= clipBounds.x)
				drawColumn(renderer, c, cx, columnWidth, clipBounds);
			cx += columnWidth + verticalBorderWidth;
		}
	}

	private void drawColumn(GSIRenderer2D renderer, int columnIndex, int columnX,
			int columnWidth, GSRectangle clipBounds) {
		
		GSCellContext context = new GSCellContext();
		GSRectangle bounds = new GSRectangle();
		bounds.x = columnX;
		bounds.width = columnWidth;
		bounds.y = horizontalBorderHeight;
		for (int r = 0; r < model.getRowCount() && bounds.y < clipBounds.y + clipBounds.height; r++) {
			bounds.height = getRowHeight(r);
			if (bounds.y + bounds.height >= clipBounds.y) {
				initCellContext(context, columnIndex, r, bounds);
				drawCell(renderer, columnIndex, r, context);
			}
			bounds.y += bounds.height + horizontalBorderHeight;
		}
	}
	
	private void drawCell(GSIRenderer2D renderer, int columnIndex, int rowIndex, GSCellContext context) {
		drawCell(renderer, model.getCellValue(columnIndex, rowIndex), context);
	}

	private <T> void drawCell(GSIRenderer2D renderer, T cellValue, GSCellContext context) {
		getCellRenderer(cellValue).render(renderer, cellValue, context);
	}

	private void drawBorders(GSIRenderer2D renderer, GSRectangle clipBounds) {
		int color = isEnabled() ? borderColor : disabledBorderColor;
		if (GSColorUtil.unpackA(color) != 0x00)
			drawBorder(renderer, clipBounds, color);
		// Check if we have a selection, and draw the corresponding borders
		if (GSColorUtil.unpackA(selectionBorderColor) != 0x00) {
			GSRectangle bounds = getSelectionBounds();
			if (bounds != null) {
				bounds = bounds.intersection(clipBounds);
				drawBorder(renderer, bounds, selectionBorderColor);
			}
			if (isSelectionEnabled()) {
				// Draw border around hovered cell
				int mx = renderer.getMouseX(), my = renderer.getMouseY();
				if (clipBounds.contains(mx, my)) {
					int c = getColumnIndexAtX(mx), r = getRowIndexAtY(my);
					int hoverColor = GSColorUtil.brighter(selectionBackgroundColor);
					drawBorder(renderer, getCellBounds(c, r), hoverColor);
				}
			}
		}
	}

	private void drawBorder(GSIRenderer2D renderer, GSRectangle clipBounds, int color) {
		if (verticalBorderWidth != 0) {
			// Compute total height of rows
			int h = Math.min(clipBounds.height, getRowY(model.getRowCount()));
			// Draw border lines
			int c0 = getColumnIndexAtX(Math.max(0, clipBounds.x));
			if (c0 != -1) {
				int x = getColumnX(c0);
				for (int c = c0; c <= model.getColumnCount() && x < clipBounds.x + clipBounds.width; c++) {
					renderer.fillRect(x, clipBounds.y, verticalBorderWidth, h, color);
					x += verticalBorderWidth;
					// The last line does not have a following column.
					if (c != model.getColumnCount())
						x += getColumnWidth(c);
				}
			}
		}
		if (horizontalBorderHeight != 0) {
			// Compute total width of columns
			int w = Math.min(clipBounds.width, getColumnX(model.getColumnCount()));
			// Draw border lines
			int r0 = getRowIndexAtY(Math.max(0, clipBounds.y));
			if (r0 != -1) {
				int y = getRowY(r0);
				for (int r = r0; r <= model.getRowCount() && y < clipBounds.y + clipBounds.height; r++) {
					renderer.fillRect(clipBounds.x, y, w, horizontalBorderHeight, color);
					y += horizontalBorderHeight;
					// The last line does not have a following row.
					if (r != model.getRowCount())
						y += getRowHeight(r);
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
			accX += verticalBorderWidth;
			accX += getColumnWidth(index);
		}
		return index;
	}
	
	public int getColumnX(int index) {
		int accX = 0;
		// Compute first index that we do not consider
		int cmx = Math.min(index, model.getColumnCount());
		int c = 0;
		for ( ; c < cmx; c++) {
			accX += verticalBorderWidth;
			accX += getColumnWidth(c);
		}
		if (c >= model.getColumnCount()) {
			// Include the last border line
			accX += verticalBorderWidth;
		}
		return accX;
	}
	
	private int getColumnWidth(int index) {
		return model.getColumn(index).getWidth();
	}

	public int getRowIndexAtY(int y) {
		int index = -1, accY = 0;
		while (accY <= y) {
			index++;
			if (index >= model.getRowCount())
				return -1;
			accY += horizontalBorderHeight;
			accY += getRowHeight(index);
		}
		return index;
	}
	
	public int getRowY(int index) {
		int accY = 0;
		// Compute first index that we do not consider
		int rmx = Math.min(index, model.getRowCount());
		int r = 0;
		for ( ; r < rmx; r++) {
			accY += horizontalBorderHeight;
			accY += getRowHeight(r);
		}
		if (r >= model.getRowCount()) {
			// Include the last border line
			accY += horizontalBorderHeight;
		}
		return accY;
	}
	
	private int getRowHeight(int index) {
		return model.getRow(index).getHeight();
	}

	public GSRectangle getCellBounds(int columnIndex, int rowIndex) {
		return getCellBounds(columnIndex, rowIndex, null);
	}

	public GSRectangle getCellBounds(int columnIndex, int rowIndex, GSRectangle dest) {
		if (dest == null)
			dest = new GSRectangle();
		dest.x = getColumnX(columnIndex);
		if (columnIndex >= 0 && columnIndex < model.getColumnCount()) {
			dest.width = getColumnWidth(columnIndex) + 2 * verticalBorderWidth;
		} else {
			dest.width = 0;
		}
		dest.y = getRowY(rowIndex);
		if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
			dest.height = getRowHeight(rowIndex) + 2 * horizontalBorderHeight;
		} else {
			dest.height = 0;
		}
		return dest;
	}

	private GSRectangle getInnerCellBounds(int columnIndex, int rowIndex, GSRectangle dest) {
		if (dest == null)
			dest = new GSRectangle();
		dest.x = getColumnX(columnIndex);
		if (columnIndex >= 0 && columnIndex < model.getColumnCount()) {
			dest.x += verticalBorderWidth;
			dest.width = getColumnWidth(columnIndex);
		} else {
			dest.width = 0;
		}
		dest.y = getRowY(rowIndex);
		if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
			dest.y += horizontalBorderHeight;
			dest.height = getRowHeight(rowIndex);
		} else {
			dest.height = 0;
		}
		return dest;
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
		clearSelection();
		invalidate();
		
		dispatchModelChangedEvent();
	}
	
	public void addModelListener(GSIModelListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
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
	
	public GSIHeaderSelectionModel getColumnSelectionModel() {
		return columnSelectionModel;
	}

	public GSIHeaderSelectionModel getRowSelectionModel() {
		return rowSelectionModel;
	}
	
	public void setSelectedCells(int c0, int r0, int c1, int r1) {
		setSelectedColumns(c0, c1);
		setSelectedRows(r0, r1);
	}

	public void setSelectedColumns(int c0, int c1) {
		columnSelectionModel.setInterval(c0, c1);
	}

	public void setSelectedRows(int r0, int r1) {
		rowSelectionModel.setInterval(r0, r1);
	}
	
	public void selectAll() {
		setSelectedCells(0, model.getColumnCount(), 0, model.getRowCount());
	}

	public void clearSelection() {
		columnSelectionModel.clear();
		rowSelectionModel.clear();
	}

	public boolean isCellSelected(int columnIndex, int rowIndex) {
		if (columnSelectionModel.getSelectionPolicy() == GSEHeaderSelectionPolicy.DISABLED ||
				(columnIndex == INVALID_HEADER_INDEX && !columnSelectionModel.isEmpty())) {
			// Entire columns are selected (according to row selection)
			return rowSelectionModel.isSelected(rowIndex);
		}
		if (rowSelectionModel.getSelectionPolicy() == GSEHeaderSelectionPolicy.DISABLED ||
				(rowIndex == INVALID_HEADER_INDEX && !rowSelectionModel.isEmpty())) {
			// Entire rows are selected (according to column selection)
			return columnSelectionModel.isSelected(columnIndex);
		}
		// Otherwise, select cells in the intersection of column
		// and row selection models.
		return columnSelectionModel.isSelected(columnIndex) &&
		       rowSelectionModel.isSelected(rowIndex);
	}
	
	public boolean hasSelection() {
		if (columnSelectionModel.getSelectionPolicy() == GSEHeaderSelectionPolicy.DISABLED)
			return !rowSelectionModel.isEmpty();
		if (rowSelectionModel.getSelectionPolicy() == GSEHeaderSelectionPolicy.DISABLED)
			return !columnSelectionModel.isEmpty();
		return !(columnSelectionModel.isEmpty() || rowSelectionModel.isEmpty());
	}
	
	public GSEHeaderSelectionPolicy getColumnSelectionPolicy() {
		return columnSelectionModel.getSelectionPolicy();
	}
	
	public void setColumnSelectionPolicy(GSEHeaderSelectionPolicy policy) {
		GSEHeaderSelectionPolicy oldPolicy = columnSelectionModel.getSelectionPolicy();
		columnSelectionModel.setSelectionPolicy(policy);
		if (policy != oldPolicy && oldPolicy == GSEHeaderSelectionPolicy.DISABLED) {
			// Clear selection of rows to ensure consistency
			rowSelectionModel.clear();
		}
	}

	public GSEHeaderSelectionPolicy getRowSelectionPolicy() {
		return rowSelectionModel.getSelectionPolicy();
	}

	public void setRowSelectionPolicy(GSEHeaderSelectionPolicy policy) {
		GSEHeaderSelectionPolicy oldPolicy = rowSelectionModel.getSelectionPolicy();
		rowSelectionModel.setSelectionPolicy(policy);
		if (policy != oldPolicy && oldPolicy == GSEHeaderSelectionPolicy.DISABLED) {
			// Clear selection of columns to ensure consistency
			columnSelectionModel.clear();
		}
	}
	
	public void setCellSelectionPolicy(GSEHeaderSelectionPolicy policy) {
		setColumnSelectionPolicy(policy);
		setRowSelectionPolicy(policy);
	}
	
	public boolean isSelectionEnabled() {
		return getColumnSelectionPolicy() != GSEHeaderSelectionPolicy.DISABLED ||
		       getRowSelectionPolicy() != GSEHeaderSelectionPolicy.DISABLED;
	}
	
	private GSRectangle getSelectionBounds() {
		return getSelectionBounds(null);
	}

	private GSRectangle getSelectionBounds(GSRectangle dest) {
		if (!hasSelection())
			return null;
		if (dest == null)
			dest = new GSRectangle();
		computeColumnSelectionBounds(dest);
		computeRowSelectionBounds(dest);
		return dest;
	}
	
	/* Visible for GSTableColumnHeaderPanel. Requires: hasSelection() && dest != null */
	void computeColumnSelectionBounds(GSRectangle dest) {
		//assert(hasSelection() && dest != null)
		if (columnSelectionModel.getSelectionPolicy() != GSEHeaderSelectionPolicy.DISABLED) {
			// We must have a selection by the check above
			//assert(!columnSelectionModel.isEmpty())
			int c0 = columnSelectionModel.getIntervalMin();
			int c1 = columnSelectionModel.getIntervalMax();
			dest.x = getColumnX(c0);
			dest.width = verticalBorderWidth;
			for (int c = c0; c <= c1 && c < model.getColumnCount(); c++) {
				dest.width += verticalBorderWidth;
				dest.width += getColumnWidth(c);
			}
		} else {
			// Entire row is selected
			dest.x = 0;
			dest.width = getColumnX(model.getColumnCount());
		}
	}

	/* Visible for GSTableRowHeaderPanel. Requires: hasSelection() && dest != null */
	void computeRowSelectionBounds(GSRectangle dest) {
		//assert(hasSelection() && dest != null)
		if (rowSelectionModel.getSelectionPolicy() != GSEHeaderSelectionPolicy.DISABLED) {
			// We must have a selection by the check above
			//assert(!rowSelectionModel.isEmpty())
			int r0 = rowSelectionModel.getIntervalMin();
			int r1 = rowSelectionModel.getIntervalMax();
			dest.y = getRowY(r0);
			dest.height = horizontalBorderHeight;
			for (int r = r0; r <= r1 && r < model.getRowCount(); r++) {
				dest.height += horizontalBorderHeight;
				dest.height += getRowHeight(r);
			}
		} else {
			// Entire column is selected
			dest.y = 0;
			dest.height = getRowY(model.getRowCount());
		}
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
	
	public int getSelectionBackgroundColor() {
		return selectionBackgroundColor;
	}
	
	public void setSelectionBackgroundColor(int selectionBackgroundColor) {
		this.selectionBackgroundColor = selectionBackgroundColor;
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
	
	public int getSelectionTextColor() {
		return selectionTextColor;
	}
	
	public void setSelectionTextColor(int selectionTextColor) {
		this.selectionTextColor = selectionTextColor;
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
	
	public int getSelectionBorderColor() {
		return selectionBorderColor;
	}
	
	public void setSelectionBorderColor(int selectionBorderColor) {
		this.selectionBorderColor = selectionBorderColor;
	}

	public int getDisabledBorderColor() {
		return disabledBorderColor;
	}
	
	public void setDisabledBorderColor(int color) {
		disabledBorderColor = color;
	}
	
	@Override
	public void mousePressed(GSMouseEvent event) {
		if (event.isConsumed() || event.getButton() != GSMouseEvent.BUTTON_LEFT)
			return;
		int c = getColumnIndexAtX(event.getX());
		int r = getRowIndexAtY(event.getY());
		if (c != -1 && r != -1) {
			setSelectedCells(c, r, c, r);
			columnSelectionModel.setAnchor(c);
			rowSelectionModel.setAnchor(r);
			event.consume();
		}
	}

	@Override
	public void mouseDragged(GSMouseEvent event) {
		if (event.isConsumed() || event.getButton() != GSMouseEvent.BUTTON_LEFT)
			return;
		int c1 = getColumnIndexAtX(event.getX());
		int r1 = getRowIndexAtY(event.getY());
		if (c1 != -1 && r1 != -1) {
			int c0 = columnSelectionModel.getAnchor();
			int r0 = rowSelectionModel.getAnchor();
			setSelectedCells(c0, r0, c1, r1);
			event.consume();
		}
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
				int w = column.getWidth() + verticalBorderWidth;
				return w - delta;
			} else if (columnIndex > 0) {
				GSITableColumn prevColumn = model.getColumn(columnIndex - 1);
				int w = prevColumn.getWidth() + verticalBorderWidth;
				return w + delta;
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
				int h = row.getHeight() + horizontalBorderHeight;
				return h - delta;
			} else if (rowIndex > 0) {
				GSITableRow prevRow = model.getRow(rowIndex - 1);
				int h = prevRow.getHeight() + + horizontalBorderHeight;
				return h + delta;
			}
		}
		return GSIScrollable.super.getIncrementalScrollY(sign);
	}
	
	@Override
	public void cellValueChanged(int columnIndex, int rowIndex) {
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
