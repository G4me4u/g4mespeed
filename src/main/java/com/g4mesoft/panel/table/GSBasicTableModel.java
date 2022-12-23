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
		
		private GSDimension minimumSize;
		private boolean minimumSizeSet;
		private GSDimension maximumSize;
		private boolean maximumSizeSet;
		
		public GSAbstractHeaderElement(int index) {
			this.index = index;
			
			value = null;
			
			minimumSize = null;
			minimumSizeSet = false;
			maximumSize = null;
			maximumSizeSet = false;
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
			if (!minimumSizeSet)
				minimumSize = null;
			if (!maximumSizeSet)
				maximumSize = null;
		}
		
		@Override
		public GSDimension getMinimumSize() {
			if (minimumSize == null)
				minimumSize = calculateMinimumSize(value);
			return (minimumSize != null) ? minimumSize : GSDimension.ZERO;
		}

		private <T> GSDimension calculateMinimumSize(T value) {
			if (table != null)
				return table.getCellRenderer(value).getMinimumSize(value);
			return null;
		}
		
		@Override
		public void setMinimumSize(GSDimension minimumSize) {
			if (minimumSize == null)
				throw new IllegalArgumentException("minimumSize is null!");
			this.minimumSize = minimumSize;
			minimumSizeSet = true;
			dispatchSizeChanged();
		}

		@Override
		public GSDimension getMaximumSize() {
			if (maximumSize == null)
				maximumSize = calculateMaximumSize(value);
			return (maximumSize != null) ? maximumSize : GSDimension.ZERO;
		}

		private <T> GSDimension calculateMaximumSize(T value) {
			if (table != null)
				return table.getCellRenderer(value).getMaximumSize(value);
			return null;
		}
		
		@Override
		public void setMaximumSize(GSDimension maximumSize) {
			if (maximumSize == null)
				throw new IllegalArgumentException("maximumSize is null!");
			this.maximumSize = maximumSize;
			maximumSizeSet = true;
			dispatchSizeChanged();
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
