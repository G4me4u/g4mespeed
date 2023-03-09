package com.g4mesoft.panel.table;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSDimension;

public class GSBasicTableModel implements GSITableModel {

	private final int rowCount;
	private final int columnCount;

	private final GSBasicTableColumn[] columns;
	private final GSBasicTableRow[] rows;
	private final Object[] cellValues;
	
	private boolean columnHeaderHidden;
	private boolean rowHeaderHidden;
	
	private final List<GSITableModelListener> listeners;
	
	private GSTablePanel table;
	
	public GSBasicTableModel(int columnCount, int rowCount) {
		if (columnCount < 0 || rowCount < 0)
			throw new IllegalArgumentException("Column and row count must be positive!");
		
		this.columnCount = columnCount;
		this.rowCount = rowCount;
		
		columns = new GSBasicTableColumn[columnCount];
		rows = new GSBasicTableRow[rowCount];
		cellValues = new Object[columnCount * rowCount];

		columnHeaderHidden = false;
		rowHeaderHidden = false;
		
		listeners = new ArrayList<>();

		// Initialize columns and rows
		for (int c = 0; c < columnCount; c++)
			columns[c] = new GSBasicTableColumn(c);
		for (int r = 0; r < rowCount; r++)
			rows[r] = new GSBasicTableRow(r);
	
		table = null;
	}
	
	@Override
	public void install(GSTablePanel table) {
		if (this.table != null)
			throw new IllegalStateException("Model already installed");
		this.table = table;
		invalidateHeaders();
	}

	@Override
	public void uninstall(GSTablePanel table) {
		if (table == null || this.table != table)
			throw new IllegalStateException("Model not installed on given table");
		invalidateHeaders();
		this.table = null;
	}
	
	private void invalidateHeaders() {
		for (int c = 0; c < columnCount; c++)
			columns[c].invalidate();
		for (int r = 0; r < rowCount; r++)
			rows[r].invalidate();
	}

	@Override
	public void addListener(GSITableModelListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		listeners.add(listener);
	}

	@Override
	public void removeListener(GSITableModelListener listener) {
		listeners.remove(listener);
	}
	
	private void dispatchColumnHeaderChanged(int columnIndex) {
		for (GSITableModelListener listener : listeners)
			listener.columnHeaderChanged(columnIndex);
	}
	
	private void dispatchColumnSizeChanged(int columnIndex) {
		for (GSITableModelListener listener : listeners)
			listener.columnSizeChanged(columnIndex);
	}

	private void dispatchRowHeaderChanged(int rowIndex) {
		for (GSITableModelListener listener : listeners)
			listener.rowHeaderChanged(rowIndex);
	}
	
	private void dispatchRowSizeChanged(int rowIndex) {
		for (GSITableModelListener listener : listeners)
			listener.rowSizeChanged(rowIndex);
	}

	private void dispatchCellValueChanged(int columnIndex, int rowIndex) {
		for (GSITableModelListener listener : listeners)
			listener.cellValueChanged(columnIndex, rowIndex);
	}

	private void dispatchHeaderVisibilityChanged() {
		for (GSITableModelListener listener : listeners)
			listener.headerVisibilityChanged();
	}
	
	private void checkColumnRange(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= columnCount)
			throw new IndexOutOfBoundsException("Column index out of bounds: " + columnIndex);
	}

	private void checkRowRange(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= rowCount)
			throw new IndexOutOfBoundsException("Row index out of bounds: " + rowIndex);
	}

	private void checkCellRange(int columnIndex, int rowIndex) {
		checkColumnRange(columnIndex);
		checkRowRange(rowIndex);
	}
	
	@Override
	public GSITableColumn getColumn(int columnIndex) {
		checkColumnRange(columnIndex);
		return columns[columnIndex];
	}

	@Override
	public GSITableRow getRow(int rowIndex) {
		checkRowRange(rowIndex);
		return rows[rowIndex];
	}

	@Override
	public Object getCellValue(int columnIndex, int rowIndex) {
		checkCellRange(columnIndex, rowIndex);
		return cellValues[columnIndex + rowIndex * columnCount];
	}
	
	@Override
	public void setCellValue(int columnIndex, int rowIndex, Object value) {
		checkCellRange(columnIndex, rowIndex);
		cellValues[columnIndex + rowIndex * columnCount] = value;
		dispatchCellValueChanged(columnIndex, rowIndex);
	}
	
	@Override
	public boolean isColumnHeaderHidden() {
		return columnHeaderHidden;
	}

	@Override
	public void setColumnHeaderHidden(boolean hidden) {
		if (hidden != columnHeaderHidden) {
			columnHeaderHidden = hidden;
			dispatchHeaderVisibilityChanged();
		}
	}

	@Override
	public boolean isRowHeaderHidden() {
		return rowHeaderHidden;
	}
	
	@Override
	public void setRowHeaderHidden(boolean hidden) {
		if (hidden != rowHeaderHidden) {
			rowHeaderHidden = hidden;
			dispatchHeaderVisibilityChanged();
		}
	}
	
	@Override
	public int getColumnCount() {
		return columnCount;
	}
	
	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	private abstract class GSAbstractHeaderElement implements GSITableHeaderElement {

		protected final int index;
		
		private Object value;
		
		private GSDimension minimumSizeCache;
		private int minimumWidth;
		private int minimumHeight;
		private GSDimension maximumSizeCache;
		private int maximumWidth;
		private int maximumHeight;
		
		public GSAbstractHeaderElement(int index) {
			this.index = index;
			
			value = null;
			
			minimumSizeCache = null;
			minimumWidth = minimumHeight = -1;
			maximumSizeCache = null;
			maximumWidth = maximumHeight = -1;
		}
		
		@Override
		public Object getHeaderValue() {
			return value;
		}

		@Override
		public void setHeaderValue(Object value) {
			this.value = value;
			invalidate();
			dispatchHeaderChanged();
		}
		
		/* Visible for GSBasicTableModel */
		void invalidate() {
			minimumSizeCache = null;
			maximumSizeCache = null;
		}
		
		@Override
		public GSDimension getMinimumSize() {
			if (minimumSizeCache == null) {
				// Check if we have manually set minimum size
				if (minimumWidth != -1 && minimumHeight != -1) {
					minimumSizeCache = new GSDimension(minimumWidth, minimumHeight);
				} else {
					GSDimension mns = calculateMinimumSize(value);
					if (mns == null)
						mns = GSDimension.ZERO;
					int w = minimumWidth  != -1 ? minimumWidth  : mns.getWidth();
					int h = minimumHeight != -1 ? minimumHeight : mns.getHeight();
					minimumSizeCache = new GSDimension(w, h);
				}
			}
			return minimumSizeCache;
		}

		private <T> GSDimension calculateMinimumSize(T value) {
			if (table != null)
				return table.getCellRenderer(value).getMinimumSize(value);
			return null;
		}
		
		@Override
		public GSITableHeaderElement setMinimumSize(GSDimension minimumSize) {
			if (minimumSize == null)
				throw new IllegalArgumentException("minimumSize is null!");
			if (minimumSize.getWidth() != minimumWidth || minimumSize.getHeight() != minimumHeight) {
				minimumSizeCache = minimumSize;
				minimumWidth = minimumSize.getWidth();
				minimumHeight = minimumSize.getHeight();
				dispatchSizeChanged();
			}
			return this;
		}

		@Override
		public GSITableHeaderElement setMinimumWidth(int width) {
			if (width < 0)
				throw new IllegalArgumentException("width must be non-negative!");
			if (width != minimumHeight) {
				minimumSizeCache = null;
				minimumWidth = width;
				dispatchSizeChanged();
			}
			return this;
		}
		
		@Override
		public GSITableHeaderElement setMinimumHeight(int height) {
			if (height < 0)
				throw new IllegalArgumentException("height must be non-negative!");
			if (height != minimumHeight) {
				minimumSizeCache = null;
				minimumHeight = height;
				dispatchSizeChanged();
			}
			return this;
		}
		
		@Override
		public GSDimension getMaximumSize() {
			if (maximumSizeCache == null) {
				// Check if we have manually set minimum size
				if (maximumWidth != -1 && maximumHeight != -1) {
					maximumSizeCache = new GSDimension(maximumWidth, maximumHeight);
				} else {
					GSDimension mxs = calculateMaximumSize(value);
					if (mxs == null)
						mxs = GSDimension.ZERO;
					int w = maximumWidth  != -1 ? maximumWidth  : mxs.getWidth();
					int h = maximumHeight != -1 ? maximumHeight : mxs.getHeight();
					maximumSizeCache = new GSDimension(w, h);
				}
			}
			return maximumSizeCache;
		}

		private <T> GSDimension calculateMaximumSize(T value) {
			if (table != null)
				return table.getCellRenderer(value).getMaximumSize(value);
			return null;
		}
		
		@Override
		public GSITableHeaderElement setMaximumSize(GSDimension maximumSize) {
			if (maximumSize == null)
				throw new IllegalArgumentException("maximumSize is null!");
			if (maximumSize.getWidth() != maximumWidth || maximumSize.getHeight() != maximumHeight) {
				maximumSizeCache = maximumSize;
				maximumWidth = maximumSize.getWidth();
				maximumHeight = maximumSize.getHeight();
				dispatchSizeChanged();
			}
			return this;
		}

		@Override
		public GSITableHeaderElement setMaximumWidth(int width) {
			if (width < 0)
				throw new IllegalArgumentException("width must be non-negative!");
			if (width != maximumHeight) {
				maximumSizeCache = null;
				maximumWidth = width;
				dispatchSizeChanged();
			}
			return this;
		}
		
		@Override
		public GSITableHeaderElement setMaximumHeight(int height) {
			if (height < 0)
				throw new IllegalArgumentException("height must be non-negative!");
			if (height != maximumHeight) {
				maximumSizeCache = null;
				maximumHeight = height;
				dispatchSizeChanged();
			}
			return this;
		}
		
		protected abstract void dispatchHeaderChanged();
		
		protected abstract void dispatchSizeChanged();
		
	}
	
	private class GSBasicTableColumn extends GSAbstractHeaderElement implements GSITableColumn {

		private int width;
		
		public GSBasicTableColumn(int columnIndex) {
			super(columnIndex);
			
			width = 0;
		}
		
		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public void setWidth(int width) {
			if (width < 0)
				throw new IllegalArgumentException("width must be non-negative");
			this.width = width;
			dispatchSizeChanged();
		}

		@Override
		protected void dispatchHeaderChanged() {
			dispatchColumnHeaderChanged(index);
		}

		@Override
		protected void dispatchSizeChanged() {
			dispatchColumnSizeChanged(index);
		}
	}
	
	private class GSBasicTableRow extends GSAbstractHeaderElement implements GSITableRow {

		private int height;
		
		public GSBasicTableRow(int rowIndex) {
			super(rowIndex);
			
			height = 0;
		}
		
		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public void setHeight(int height) {
			if (height < 0)
				throw new IllegalArgumentException("height must be non-negative");
			this.height = height;
			dispatchSizeChanged();
		}

		@Override
		protected void dispatchHeaderChanged() {
			dispatchRowHeaderChanged(index);
		}

		@Override
		protected void dispatchSizeChanged() {
			dispatchRowSizeChanged(index);
		}
	}
}
