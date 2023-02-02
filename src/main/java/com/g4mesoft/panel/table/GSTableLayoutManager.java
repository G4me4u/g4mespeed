package com.g4mesoft.panel.table;

import java.util.Arrays;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSILayoutManager;
import com.g4mesoft.panel.GSParentPanel;

public class GSTableLayoutManager implements GSILayoutManager {

	/**
	 * The maximum number of elements that use the basic quadratic time
	 * worst-case size-distribution algorithm.
	 * 
	 * @see #distributeSize(GSIAccessor, int, int, int, boolean)
	 */
	private static final int QUADRATIC_DISTRIBUTION_ALG_THRESHOLD = 10;
	
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
		long wl = 0L;
		int h = 0;
		int prefCount = table.getPreferredColumnCount();
		if (prefCount == GSTablePanel.PREFERRED_COUNT_UNSPECIFIED)
			prefCount = model.getColumnCount();
		for (int c = 0; c < model.getColumnCount(); c++) {
			GSITableColumn column = model.getColumn(c);
			// Adjust visible column count for scrollable content
			if (!scrollable || c < prefCount)
				wl += computeMinimumColumnWidth(table, column);
			h = Math.max(column.getMinimumSize().getHeight(), h);
		}
		if (preferred && scrollable) {
			// Fill in remaining empty columns
			wl = Math.max(wl, table.getMinimumColumnWidth() * (long)prefCount);
		}
		// Adjust for number of border lines shown
		if (!scrollable || (!preferred && model.getColumnCount() < prefCount)) {
			wl += table.getVerticalBorderWidth() * ((long)model.getColumnCount() + 1L);
		} else {
			wl += table.getVerticalBorderWidth() * ((long)prefCount + 1L);
		}
		return new GSDimension((int)Math.min(Integer.MAX_VALUE, wl), h);
	}
	
	private static int computeMinimumColumnWidth(GSTablePanel table, GSITableColumn column) {
		return Math.max(table.getMinimumColumnWidth(), column.getMinimumSize().getWidth());
	}
	
	public static GSDimension getRowHeaderSize(GSTablePanel table, boolean preferred, boolean scrollable) {
		GSITableModel model = table.getModel();
		// Compute width (maximum of row headers), height (accumulative row height).
		int w = 0;
		long hl = 0;
		int prefCount = table.getPreferredRowCount();
		if (prefCount == GSTablePanel.PREFERRED_COUNT_UNSPECIFIED)
			prefCount = model.getRowCount();
		for (int r = 0; r < model.getRowCount(); r++) {
			GSITableRow row = model.getRow(r);
			w = Math.max(row.getMinimumSize().getWidth(), w);
			// Adjust visible row count for scrollable content
			if (!scrollable || r < prefCount)
				hl += computeMinimumRowHeight(table, row);
		}
		if (preferred && scrollable) {
			// Fill in remaining empty rows
			hl = Math.max(hl, table.getMinimumRowHeight() * (long)prefCount);
		}
		// Adjust for number of border lines shown
		if (!scrollable || (!preferred && model.getRowCount() < prefCount)) {
			hl += table.getHorizontalBorderHeight() * ((long)model.getRowCount() + 1L);
		} else {
			hl += table.getHorizontalBorderHeight() * ((long)prefCount + 1L);
		}
		return new GSDimension(w, (int)Math.min(Integer.MAX_VALUE, hl));
	}
	
	private static int computeMinimumRowHeight(GSTablePanel table, GSITableRow row) {
		return Math.max(table.getMinimumRowHeight(), row.getMinimumSize().getHeight());
	}
	
	@Override
	public void layoutChildren(GSParentPanel parent) {
		GSTablePanel table = (GSTablePanel)parent;
		
		// Layout of columns and rows is entirely independent
		layoutHeaders(new GSColumnAccessor(table), table.getWidth());
		layoutHeaders(new GSRowAccessor(table), table.getHeight());
	}
	
	private static <T extends GSITableHeaderElement> void layoutHeaders(GSIAccessor<T> accessor, int rem) {
		// Adjust for number of border lines
		rem = Math.max(0, rem - accessor.getBorderSize() * (accessor.getCount() + 1));
		
		// Phase 1: perform basic bounds check to ensure that the
		//          the headers have at most maximum and at least
		//          minimum size (important for later algorithms)
		for (int i = 0; i < accessor.getCount(); i++) {
			T e = accessor.getElement(i);
			int s = accessor.getSize(e);
			s = Math.min(s, accessor.getMaximumSize(e));
			s = Math.max(s, accessor.getMinimumSize(e));
			accessor.setSize(e, s);
			rem -= s;
		}
		// Phase 2: distribute remaining size according to resize policy
		int resizingIndex = accessor.getResizingIndex();
		int i0 = accessor.getCount(), i1 = accessor.getCount();
		switch (accessor.getResizePolicy()) {
		case RESIZE_LAST:
			i0 = i1 - 1;
			break;
		case RESIZE_SUBSEQUENT:
			if (resizingIndex != -1)
				i0 = resizingIndex + 1;
			break;
		case RESIZE_NEXT:
			if (resizingIndex != -1) {
				i0 = resizingIndex + 1;
				// Note: ensure we have a next header element.
				i1 = Math.min(resizingIndex + 2, i1);
			}
			break;
		case RESIZE_OFF:
			break;
		case RESIZE_ALL:
		default:
			i0 = 0;
			break;
		}
		rem = distributeSize(accessor, rem, i0, i1, true);
		// Only resize if we can not fit or resize policy is not off.
		if (rem < 0 || accessor.getResizePolicy() != GSEHeaderResizePolicy.RESIZE_OFF) {
			// Phase 3: take remaining size from resizing element.
			if (resizingIndex != -1)
				rem = distributeSize(accessor, rem, resizingIndex, resizingIndex + 1, true);
			// Phase 4: distribute remaining size among all remaining header elements
			rem = distributeSize(accessor, rem, 0, accessor.getCount(), true);
			rem = distributeSize(accessor, rem, 0, accessor.getCount(), false);
			//assert rem >= 0
		}
	}
	
	private static <T extends GSITableHeaderElement> int distributeSize(GSIAccessor<T> accessor, int rem, int i0, int i1, boolean mnsFlag) {
		// Number of elements that need to be laid out
		int remCnt = i1 - i0;
		if (rem == 0 || remCnt <= 0) {
			// Nothing to distribute
			return rem;
		}
		
		int sign = rem < 0 ? -1 : 1;
		if (remCnt <= QUADRATIC_DISTRIBUTION_ALG_THRESHOLD || (rem < 0 && !mnsFlag)) {
			// Use a simple O(n^2) worst-case algorithm to distribute the size.
			// Although the algorithm is worst-case quadratic, many instances will
			// incur close to linear time. In particular, if the table has Q headers
			// with a reached threshold, the algorithm runs in O(n Q) worst-case.
			//
			// The algorithm works as follows:
			//     1. Distribute the remaining size equally among all elements.
			//     2. If there is still size to distribute, repeat 1) on the
			//        elements that have not reached their threshold.
			boolean first = true;
			do {
				int curRem = rem;
				int curRemCnt = remCnt;
				for (int i = i0; i < i1; i++) {
					T e = accessor.getElement(i);
					int s = accessor.getSize(e);
					// Compute absolute threshold
					int t;
					if (sign < 0) {
						t = mnsFlag ? s - accessor.getMinimumSize(e) : s;
					} else {
						t = accessor.getMaximumSize(e) - s;
					}
					// First pass should consider all, since threshold might already
					// be zero in the first iteration (and we do not know how many).
					if (first || t != 0) {
						int ds = curRem / curRemCnt;
						curRem -= ds;
						curRemCnt--;
						// Check if we reached the threshold
						if (sign * ds /* abs(ds) */ >= t) {
							ds = sign * t;
							// t == 0 next round
							remCnt--;
						}
						accessor.setSize(e, s + ds);
						rem -= ds;
					}
				}
				first = false;
			} while (rem != 0 && remCnt > 0);
		} else {
			// Use O(n log n) worst-case algorithm. This leads to linear heap-space
			// allocation, which might be slow and is definitely not worth it for
			// some data sets.
			//
			// The algorithm works as follows:
			//    1. Compute the thresholds that each element can receive, in the
			//       sign indicated by the remaining size (since we can only hit one
			//       of the two thresholds, and it's always determined by the sign).
			//    2. Go through the sorted thresholds to compute the maximum threshold
			//       that is reached. That is, compute the boundary between the elements
			//       that reach their threshold and those that do not. The latter will
			//       always receive the same amount of size (that is, the average).
			//    3. Distribute the size in a one-pass algorithm (since we can now in
			//       constant time determine if an element will reach its threshold).
			
			// 1. Compute the maximum (absolute) size delta for each of the header elements.
			int[] thresholds = new int[remCnt];
			for (int i = i0; i < i1; i++) {
				T e = accessor.getElement(i);
				int s = accessor.getSize(e);
				if (sign < 0) {
					thresholds[i - i0] = mnsFlag ? s - accessor.getMinimumSize(e) : s;
				} else {
					thresholds[i - i0] = accessor.getMaximumSize(e) - s;
				}
			}
			Arrays.sort(thresholds);
			// 2. Compute the globally maximum size delta that is actually reached.
			int threshold = 0;
			int remA = sign * rem;
			for (int i = i0; i < i1; i++) {
				int ds = (thresholds[i - i0] - threshold) * (i1 - i);
				remA -= ds;
				if (remA < 0)
					break;
				// Include equal thresholds (i.e. when remA == 0)
				threshold = thresholds[i - i0];
				remCnt--;
			}
			// 3. Distribute the size to header elements respecting threshold
			rem -= sign * threshold * (i1 - i0 - remCnt);
			for (int i = i0; i < i1; i++) {
				T e = accessor.getElement(i);
				int s = accessor.getSize(e);
				// Compute element threshold
				int t;
				if (sign < 0) {
					t = mnsFlag ? s - accessor.getMinimumSize(e) : s;
				} else {
					t = accessor.getMaximumSize(e) - s;
				}
				// Compute size delta
				int ds;
				if (t <= threshold) {
					// Threshold was reached
					ds = sign * t;
				} else {
					// Others get (truncated) average of remaining size
					ds = rem / remCnt;
					rem -= ds;
					remCnt--;
				}
				accessor.setSize(e, s + ds);
			}
			//assert remCnt == 0
		}
		return rem;
	}
	
	private static interface GSIAccessor<T extends GSITableHeaderElement> {
		
		public T getElement(int index);
		
		public int getCount();
		
		public int getSize(T element);
		
		/* update corresponding dimension (column: width, row: height) */
		public void setSize(T element, int size);
		
		public int getMinimumSize(T element);

		public int getMaximumSize(T element);
		
		public int getResizingIndex();

		public int getBorderSize();
		
		public GSEHeaderResizePolicy getResizePolicy();
		
	}
	
	private static class GSColumnAccessor implements GSIAccessor<GSITableColumn> {
		
		private final GSTablePanel table;
		private final GSITableModel model;
		
		public GSColumnAccessor(GSTablePanel table) {
			this.table = table;
			this.model = table.getModel();
		}
		
		@Override
		public GSITableColumn getElement(int index) {
			return model.getColumn(index);
		}
		
		@Override
		public int getCount() {
			return model.getColumnCount();
		}
		
		@Override
		public int getSize(GSITableColumn column) {
			return column.getWidth();
		}

		@Override
		public void setSize(GSITableColumn column, int width) {
			column.setWidth(width);
		}
		
		@Override
		public int getMinimumSize(GSITableColumn element) {
			return computeMinimumColumnWidth(table, element);
		}
		
		@Override
		public int getMaximumSize(GSITableColumn element) {
			// It is a strong requirement that minimum size is less
			// than or equal to maximum size. Enforce it here.
			return Math.max(getMinimumSize(element), element.getMaximumSize().getWidth());
		}
		
		@Override
		public int getResizingIndex() {
			return table.getResizingColumnIndex();
		}

		@Override
		public int getBorderSize() {
			return table.getVerticalBorderWidth();
		}
		
		@Override
		public GSEHeaderResizePolicy getResizePolicy() {
			return table.getColumnHeaderResizePolicy();
		}
	}

	private static class GSRowAccessor implements GSIAccessor<GSITableRow> {
		
		private final GSTablePanel table;
		private final GSITableModel model;
		
		public GSRowAccessor(GSTablePanel table) {
			this.table = table;
			this.model = table.getModel();
		}
		
		@Override
		public GSITableRow getElement(int index) {
			return model.getRow(index);
		}
		
		@Override
		public int getCount() {
			return model.getRowCount();
		}

		@Override
		public int getSize(GSITableRow row) {
			return row.getHeight();
		}
		
		@Override
		public void setSize(GSITableRow row, int height) {
			row.setHeight(height);
		}
		
		@Override
		public int getMinimumSize(GSITableRow element) {
			return computeMinimumRowHeight(table, element);
		}
		
		@Override
		public int getMaximumSize(GSITableRow element) {
			// It is a strong requirement that minimum size is less
			// than or equal to maximum size. Enforce it here.
			return Math.max(getMinimumSize(element), element.getMaximumSize().getHeight());
		}
		
		@Override
		public int getResizingIndex() {
			return table.getResizingRowIndex();
		}
		
		@Override
		public int getBorderSize() {
			return table.getHorizontalBorderHeight();
		}
		
		@Override
		public GSEHeaderResizePolicy getResizePolicy() {
			return table.getRowHeaderResizePolicy();
		}
	}
}
