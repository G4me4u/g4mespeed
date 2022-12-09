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
		int c = 0;
		for ( ; c < model.getColumnCount(); c++) {
			GSITableColumn column = model.getColumn(c);
			GSDimension size = preferred ? column.getPreferredSize() : column.getMinimumSize();
			// Adjust visible column count for scrollable content
			if (!scrollable || c < table.getPreferredColumnCount())
				w += Math.max(size.getWidth(), table.getMinimumColumnWidth());
			h = Math.max(size.getHeight(), h);
		}
		if (preferred && scrollable) {
			// Fill in remaining empty columns
			for ( ; c < table.getPreferredColumnCount(); c++)
				w += table.getMinimumColumnWidth();
		}
		return new GSDimension(w, h);
	}
	
	public static GSDimension getRowHeaderSize(GSTablePanel table, boolean preferred, boolean scrollable) {
		GSITableModel model = table.getModel();
		// Compute width (maximum of row headers), height (accumulative row height).
		int w = 0, h = 0;
		int r = 0;
		for ( ; r < model.getRowCount(); r++) {
			GSITableRow row = model.getRow(r);
			GSDimension size = preferred ? row.getPreferredSize() : row.getMinimumSize();
			w = Math.max(size.getWidth(), w);
			// Adjust visible row count for scrollable content
			if (!scrollable || r < table.getPreferredRowCount())
				h += Math.max(size.getHeight(), table.getMinimumRowHeight());
		}
		if (preferred && scrollable) {
			// Fill in remaining empty rows
			for ( ; r < table.getPreferredRowCount(); r++)
				h += table.getMinimumRowHeight();
		}
		return new GSDimension(w, h);
	}

	@Override
	public void layoutChildren(GSParentPanel parent) {
		GSTablePanel table = (GSTablePanel)parent;
		GSITableModel model = table.getModel();
		
		// TODO: respect resize policy!
		int remW = table.getWidth(), remH = table.getHeight();
		
		boolean preferred = true;
		GSDimension targetSize = getTableSize(table, true, false);
		if (targetSize.getWidth() > remW || targetSize.getHeight() > remH) {
			targetSize = getTableSize(table, false, false);
			preferred = false;
		}
		
		remW -= targetSize.getWidth();
		remH -= targetSize.getHeight();

		for (int c = 0; c < model.getColumnCount(); c++) {
			GSITableColumn column = model.getColumn(c);
			int cw = (preferred ? column.getPreferredSize() : column.getMinimumSize()).getWidth();
			cw = Math.max(table.getMinimumColumnWidth(), cw);
			int aw = Math.max(remW / (model.getColumnCount() - c), -cw);
			column.setWidth(cw + aw);
			remW -= aw;
		}

		for (int r = 0; r < model.getRowCount(); r++) {
			GSITableRow row = model.getRow(r);
			int rh = (preferred ? row.getPreferredSize() : row.getMinimumSize()).getHeight();
			rh = Math.max(table.getMinimumRowHeight(), rh);
			int ah = Math.max(remH / (model.getRowCount() - r), -rh);
			row.setHeight(rh + ah);
			remH -= ah;
		}
	}
}
