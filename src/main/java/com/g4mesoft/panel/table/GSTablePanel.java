package com.g4mesoft.panel.table;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSILayoutManager;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.scroll.GSIScrollable;
import com.g4mesoft.panel.scroll.GSScrollPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.Text;

public class GSTablePanel extends GSParentPanel implements GSIScrollable, GSITableModelListener {

	private static final int DEFAULT_PREFERRED_COLUMN_COUNT = Integer.MAX_VALUE;
	private static final int DEFAULT_PREFERRED_ROW_COUNT    = 10;
	private static final int DEFAULT_MINIMUM_COLUMN_WIDTH   = 30;
	private static final int DEFAULT_BACKGROUND_COLOR       = 0xFF202020;
	private static final int DEFAULT_TEXT_COLOR             = 0xFFE0E0E0;
	
	private GSITableModel model;
	
	private int preferredColumnCount;
	private int preferredRowCount;
	private int minimumColumnWidth;
	private int minimumRowHeight;
	
	private GSEHeaderResizePolicy columnHeaderResizePolicy;
	private GSEHeaderResizePolicy rowHeaderResizePolicy;
	
	private Map<Class<?>, GSITableCellRenderer<?>> cellRendererByClass;
	/* Cache for derived cell renderers (inherited from classes and interfaces) */
	private Map<Class<?>, GSITableCellRenderer<?>> cellRendererCache;
	
	private int backgroundColor;
	private int textColor;

	private int resizingColumnIndex;
	private int resizingRowIndex;

	private GSTableColumnHeaderPanel columnHeader;
	private GSTableRowHeaderPanel rowHeader;
	
	public GSTablePanel(int columnCount, int rowCount) {
		init(new GSBasicTableModel(columnCount, rowCount));
	}
	
	public GSTablePanel(GSITableModel model) {
		init(model);
	}
	
	private void init(GSITableModel model) {
		super.setLayoutManager(new GSTableLayoutManager());
		
		setModel(model);
		preferredColumnCount = DEFAULT_PREFERRED_COLUMN_COUNT;
		preferredRowCount = DEFAULT_PREFERRED_ROW_COUNT;
		minimumColumnWidth = DEFAULT_MINIMUM_COLUMN_WIDTH;
		minimumRowHeight = GSPanelContext.getRenderer().getLineHeight();
		
		columnHeaderResizePolicy = GSEHeaderResizePolicy.RESIZE_SUBSEQUENT;
		rowHeaderResizePolicy = GSEHeaderResizePolicy.RESIZE_OFF;
		
		cellRendererByClass = new IdentityHashMap<>();
		cellRendererCache = new IdentityHashMap<>();

		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		textColor = DEFAULT_TEXT_COLOR;
		
		resizingColumnIndex = resizingRowIndex = -1;
		
		initDefaultCellRenderers();
	}
	
	private void initDefaultCellRenderers() {
		setCellRenderer(String.class, GSStringTableCellRenderer.INSTANCE);
		setCellRenderer(Text.class, GSTextTableCellRenderer.INSTANCE);
		setCellRenderer(Date.class, GSDateTableCellRenderer.INSTANCE);
	}
	
	public <T> void setCellRenderer(Class<? extends T> valueClazz, GSITableCellRenderer<T> cellRenderer) {
		if (cellRenderer == null) {
			cellRendererByClass.remove(valueClazz);
		} else {
			cellRendererByClass.put(valueClazz, cellRenderer);
		}
		// Clear the cell renderer cache
		cellRendererCache.clear();
	}
	
	public <T> GSITableCellRenderer<T> getCellRenderer(T value) {
		return value == null ? GSEmptyCellRenderer.getInstance() : getCellRendererImpl(value.getClass());
	}
	
	@SuppressWarnings("unchecked")
	private <T> GSITableCellRenderer<T> getCellRendererImpl(Class<?> clazz) {
		GSITableCellRenderer<?> cellRenderer = cellRendererCache.get(clazz);
		if (cellRenderer == null) {
			// Search for closest cell renderer, where:
			//    1. A directly assigned cell renderer takes precedence
			//    2. Then direct interfaces, by DFS order, take precedence.
			//    3. Finally, if the direct interfaces do not derive a cell
			//       renderer, we recursively return the cell renderer of
			//       the super class.
			Deque<Class<?>> s = new ArrayDeque<>();
			// 1) The root of the DFS is clazz
			s.push(clazz);
			// Search through direct interfaces and their inherited
			// interfaces using DFS.
			// Note: no need to check for visited nodes, since inheritance
			//       is always a DAG. We *might* visit the same node many
			//       times, however this expensive operation happens once.
			Class<?> c;
			while ((c = s.poll()) != null) {
				cellRenderer = cellRendererByClass.get(c);
				if (cellRenderer != null)
					break;
				// 2) add direct interfaces to the stack
				for (Class<?> ic : c.getInterfaces())
					s.push(ic);
			}
			if (cellRenderer == null) {
				// 3) Recursively search the super-class.
				c = clazz.getSuperclass();
				if (c != null) {
					cellRenderer = getCellRendererImpl(c);
				} else {
					// clazz is primitive type or Object, and has no
					// assigned cell renderer.
					cellRenderer = GSEmptyCellRenderer.getInstance();
				}
			}
			//assert cellRenderer != null
			// Add the cell renderer to the cache
			cellRendererCache.put(clazz, cellRenderer);
		}
		return (GSITableCellRenderer<T>)cellRenderer;
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
		updateHeaderVisibility(false, false);
		
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

		if ((backgroundColor & 0xFF000000) != 0)
			renderer.fillRect(0, 0, width, height, backgroundColor);
		
		int cx = 0;
		for (int c = 0; c < model.getColumnCount(); c++) {
			GSITableColumn column = model.getColumn(c);
			drawColumn(renderer, c, cx, column.getWidth());
			cx += column.getWidth();
		}
	}
	
	private void drawColumn(GSIRenderer2D renderer, int columnIndex, int columnX, int columnWidth) {
		GSRectangle bounds = new GSRectangle();
		bounds.x = columnX;
		bounds.width = columnWidth;
		bounds.y = 0;
		for (int r = 0; r < model.getRowCount(); r++) {
			GSITableRow row = model.getRow(r);
			bounds.height = row.getHeight();
			drawCell(renderer, columnIndex, r, bounds);
			bounds.y += bounds.height;
		}
	}
	
	private void drawCell(GSIRenderer2D renderer, int columnIndex, int rowIndex, GSRectangle bounds) {
		drawCell(renderer, model.getCellValue(columnIndex, rowIndex), bounds);
	}

	private <T> void drawCell(GSIRenderer2D renderer, T cellValue, GSRectangle bounds) {
		getCellRenderer(cellValue).render(renderer, cellValue, bounds, this);
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

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
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
