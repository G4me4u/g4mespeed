package com.g4mesoft.panel.table;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSILayoutManager;
import com.g4mesoft.panel.GSParentPanel;

public class GSTableLayoutManager implements GSILayoutManager {

	@Override
	public GSDimension getMinimumSize(GSParentPanel parent) {
		return getTableSize((GSTablePanel)parent, false, false);
	}

	@Override
	public GSDimension getPreferredSize(GSParentPanel parent) {
		return getTableSize((GSTablePanel)parent, true, false);
	}

	public static GSDimension getTableSize(GSTablePanel table, boolean preferred, boolean scrollable) {
		GSDimension chs = getColumnHeaderSize(table, preferred, scrollable);
		GSDimension rhs = getRowHeaderSize(table, preferred, scrollable);
		// The size of the table always matches the width and
		// height of the column and row headers, respectively.
		return new GSDimension(chs.getWidth(), rhs.getHeight());
	}
	
	public static GSDimension getColumnHeaderSize(GSTablePanel table, boolean preferred, boolean scrollable) {
		GSITableModel model = table.getModel();
		// Compute width (accumulative column width), height (maximum of column headers).
		int w = 0, h = 0;
		for (int c = 0; c < model.getColumnCount(); c++) {
			GSITableColumn column = model.getColumn(c);
			// Adjust visible column count for scrollable content
			if (!scrollable || c < table.getPreferredColumnCount())
				w += computeMinimumColumnWidth(table, column);
			h = Math.max(column.getMinimumSize().getHeight(), h);
		}
		if (preferred && scrollable) {
			// Fill in remaining empty columns
			w = Math.max(w, table.getMinimumColumnWidth() * table.getPreferredColumnCount());
		}
		return new GSDimension(w, h);
	}
	
	public static GSDimension getRowHeaderSize(GSTablePanel table, boolean preferred, boolean scrollable) {
		GSITableModel model = table.getModel();
		// Compute width (maximum of row headers), height (accumulative row height).
		int w = 0, h = 0;
		for (int r = 0; r < model.getRowCount(); r++) {
			GSITableRow row = model.getRow(r);
			w = Math.max(row.getMinimumSize().getWidth(), w);
			// Adjust visible row count for scrollable content
			if (!scrollable || r < table.getPreferredRowCount())
				h += computeMinimumRowHeight(table, row);
		}
		if (preferred && scrollable) {
			// Fill in remaining empty rows
			h = Math.max(h, table.getMinimumRowHeight() * table.getPreferredRowCount());
		}
		return new GSDimension(w, h);
	}

	@Override
	public void layoutChildren(GSParentPanel parent) {
		GSTablePanel table = (GSTablePanel)parent;
		GSITableModel model = table.getModel();
		
		// Phase 1: ensure columns and rows have at least minimum size
		int remW = table.getWidth();
		for (int c = 0; c < model.getColumnCount(); c++) {
			GSITableColumn column = model.getColumn(c);
			int mnw = computeMinimumColumnWidth(table, column);
			if (mnw > column.getWidth())
				column.setWidth(mnw);
			remW -= column.getWidth();
		}
		// Phase 2: distribute remaining width according to resize policy
		int resizingColumnIndex = table.getResizingColumnIndex();
		int c0 = model.getColumnCount(), c1 = model.getColumnCount();
		switch (table.getColumnHeaderResizePolicy()) {
		case RESIZE_LAST:
			c0 = c1 - 1;
			break;
		case RESIZE_SUBSEQUENT:
			if (resizingColumnIndex != -1)
				c0 = resizingColumnIndex + 1;
			break;
		case RESIZE_NEXT:
			if (resizingColumnIndex != -1) {
				c0 = resizingColumnIndex + 1;
				// Note: ensure we have a next column.
				c1 = Math.min(resizingColumnIndex + 2, c1);
			}
			break;
		case RESIZE_OFF:
			break;
		case RESIZE_ALL:
		default:
			c0 = 0;
			break;
		}
		remW = distributeWidth(table, remW, c0, c1, true);
		// Only resize if we can not fit or resize policy is not off.
		if (remW < 0 || table.getColumnHeaderResizePolicy() != GSEHeaderResizePolicy.RESIZE_OFF) {
			// Phase 3: take remaining size from resizing column and/or row.
			if (resizingColumnIndex != -1 && remW != 0)
				remW = distributeWidth(table, remW, resizingColumnIndex, resizingColumnIndex + 1, true);
			// Phase 4: distribute remaining width among all remaining columns and rows
			if (remW != 0)
				distributeWidth(table, remW, 0, model.getColumnCount(), false);
		}
		
		// Below is the same code but for rows...
		// Phase 1
		int remH = table.getHeight();
		for (int r = 0; r < model.getRowCount(); r++) {
			GSITableRow row = model.getRow(r);
			int mnh = computeMinimumRowHeight(table, row);
			if (mnh > row.getHeight())
				row.setHeight(mnh);
			remH -= row.getHeight();
		}
		// Phase 2
		int resizingRowIndex = table.getResizingRowIndex();
		int r0 = model.getRowCount(), r1 = model.getRowCount();
		switch (table.getRowHeaderResizePolicy()) {
		case RESIZE_LAST:
			r0 = r1 - 1;
			break;
		case RESIZE_SUBSEQUENT:
			if (resizingRowIndex != -1)
				r0 = resizingRowIndex + 1;
			break;
		case RESIZE_NEXT:
			if (resizingRowIndex != -1) {
				r0 = resizingRowIndex + 1;
				// Note: ensure we have a next row.
				r1 = Math.min(resizingRowIndex + 2, r1);
			}
			break;
		case RESIZE_OFF:
			break;
		case RESIZE_ALL:
		default:
			r0 = 0;
			break;
		}
		remH = distributeHeight(table, remH, r0, r1, true);
		// Only resize if we can not fit or resize policy is not off.
		if (remH < 0 || table.getRowHeaderResizePolicy() != GSEHeaderResizePolicy.RESIZE_OFF) {
			// Phase 3
			if (resizingRowIndex != -1 && remH != 0)
				remH = distributeHeight(table, remH, resizingRowIndex, resizingRowIndex + 1, true);
			// Phase 4
			if (remH != 0)
				distributeHeight(table, remH, 0, model.getRowCount(), false);
		}
	}
	
	private static int distributeWidth(GSTablePanel table, int remW, int c0, int c1, boolean respectMinWidth) {
		GSITableModel model = table.getModel();
		for (int c = c0; c < c1; c++) {
			GSITableColumn column = model.getColumn(c);
			// Respect minimum width
			int mnw = respectMinWidth ? computeMinimumColumnWidth(table, column) : 0;
			// Always respect maximum width
			int mxw = column.getMaximumSize().getWidth();
			// Compute width distributed to current column
			int aw = remW / (c1 - c);
			aw = Math.min(aw, mxw - column.getWidth());
			aw = Math.max(aw, mnw - column.getWidth());
			// Update
			column.setWidth(column.getWidth() + aw);
			remW -= aw;
		}
		return remW;
	}
	
	private static int computeMinimumColumnWidth(GSTablePanel table, GSITableColumn column) {
		return Math.max(table.getMinimumColumnWidth(), column.getMinimumSize().getWidth());
	}
	
	private static int distributeHeight(GSTablePanel table, int remH, int r0, int r1, boolean respectMinHeight) {
		GSITableModel model = table.getModel();
		for (int r = r0; r < r1; r++) {
			GSITableRow row = model.getRow(r);
			// Respect minimum height
			int mnh = respectMinHeight ? computeMinimumRowHeight(table, row) : 0;
			// Always respect maximum height
			int mxh = row.getMaximumSize().getHeight();
			// Compute height distributed to current row
			int ah = remH / (r1 - r);
			ah = Math.min(ah, mxh - row.getHeight());
			ah = Math.max(ah, mnh - row.getHeight());
			// Update
			row.setHeight(row.getHeight() + ah);
			remH -= ah;
		}
		return remH;
	}
	
	private static int computeMinimumRowHeight(GSTablePanel table, GSITableRow row) {
		return Math.max(table.getMinimumRowHeight(), row.getMinimumSize().getHeight());
	}
}
