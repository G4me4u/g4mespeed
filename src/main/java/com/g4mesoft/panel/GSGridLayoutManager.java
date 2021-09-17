package com.g4mesoft.panel;

import java.util.List;

import com.g4mesoft.util.GSMathUtil;

public class GSGridLayoutManager implements GSILayoutManager {

	/* Following are the layout properties that are supported by this layout manager. */
	
	public static final GSILayoutProperty<Integer>     MINIMUM_WIDTH    = GSLayoutProperties.MINIMUM_WIDTH;
	public static final GSILayoutProperty<Integer>     MINIMUM_HEIGHT   = GSLayoutProperties.MINIMUM_HEIGHT;
	public static final GSILayoutProperty<GSDimension> MINIMUM_SIZE     = GSLayoutProperties.MINIMUM_SIZE;
	public static final GSILayoutProperty<Integer>     PREFERRED_WIDTH  = GSLayoutProperties.PREFERRED_WIDTH;
	public static final GSILayoutProperty<Integer>     PREFERRED_HEIGHT = GSLayoutProperties.PREFERRED_HEIGHT;
	public static final GSILayoutProperty<GSDimension> PREFERRED_SIZE   = GSLayoutProperties.PREFERRED_SIZE;

	public static final GSILayoutProperty<Integer>     GRID_X           = GSLayoutProperties.GRID_X;
	public static final GSILayoutProperty<Integer>     GRID_Y           = GSLayoutProperties.GRID_Y;
	public static final GSILayoutProperty<Integer>     GRID_WIDTH       = GSLayoutProperties.GRID_WIDTH;
	public static final GSILayoutProperty<Integer>     GRID_HEIGHT      = GSLayoutProperties.GRID_HEIGHT;
	public static final GSILayoutProperty<Float>       WEIGHT_X         = GSLayoutProperties.WEIGHT_X;
	public static final GSILayoutProperty<Float>       WEIGHT_Y         = GSLayoutProperties.WEIGHT_Y;
	
	public static final GSILayoutProperty<GSEAnchor> ANCHOR             = GSLayoutProperties.ANCHOR;
	public static final GSILayoutProperty<GSEFill>   FILL               = GSLayoutProperties.FILL;

	public static final GSILayoutProperty<Integer> TOP_MARGIN           = GSLayoutProperties.TOP_MARGIN;
	public static final GSILayoutProperty<Integer> LEFT_MARGIN          = GSLayoutProperties.LEFT_MARGIN;
	public static final GSILayoutProperty<Integer> BOTTOM_MARGIN        = GSLayoutProperties.BOTTOM_MARGIN;
	public static final GSILayoutProperty<Integer> RIGHT_MARGIN         = GSLayoutProperties.RIGHT_MARGIN;

	public static final GSILayoutProperty<GSVertMargin>  VERT_MARGIN    = GSLayoutProperties.VERT_MARGIN;
	public static final GSILayoutProperty<GSHorizMargin> HORIZ_MARGIN   = GSLayoutProperties.HORIZ_MARGIN;
	public static final GSILayoutProperty<GSMargin>      MARGIN         = GSLayoutProperties.MARGIN;
	
	@Override
	public GSDimension getMinimumSize(GSParentPanel parent) {
		return getMinimumSize(calculateLayoutInfo(parent, MINIMUM_SIZE));
	}

	@Override
	public GSDimension getPreferredSize(GSParentPanel parent) {
		return getMinimumSize(calculateLayoutInfo(parent, PREFERRED_SIZE));
	}

	@Override
	public void layoutChildren(GSParentPanel parent) {
		List<GSPanel> children = parent.getChildren();
		
		// Ensure we actually have children to lay out
		if (children.isEmpty())
			return;
		
		// Figure out if the layout can fit in preferred size or minimum size.
		GSGridLayoutInfo layoutInfo = calculateLayoutInfo(parent, PREFERRED_SIZE);
		GSDimension size = getMinimumSize(layoutInfo);

		if (size.getWidth() > parent.getWidth() || size.getHeight() > parent.getHeight()) {
			calculateLayoutInfo(parent, MINIMUM_SIZE);
			size = getMinimumSize(layoutInfo);
		}
		
		int deltaW = parent.getWidth()  - size.getWidth();
		int deltaH = parent.getHeight() - size.getHeight();

		// Distribute the width weighted to all columns
		int remW = 0;
		float remWx = layoutInfo.columnWeight;
		for (int c = 0; remWx >= GSMathUtil.EPSILON_F && c < layoutInfo.columnCount; c++) {
			float wx = layoutInfo.columnWeights[c];
			int dw = (int)(deltaW * wx / remWx);
			// Ensure that we do not get negative width
			if (layoutInfo.minColumnWidths[c] + dw < 0)
				dw = -layoutInfo.minColumnWidths[c];
			layoutInfo.minColumnWidths[c] += dw;
			// Accumulate the total width of the updated column widths
			remW += layoutInfo.minColumnWidths[c];
			remWx -= wx;
			deltaW -= dw;
		}
		
		if (deltaW < 0) {
			// The weighted columns did not have enough width to give,
			// take it equally from the columns with no weight.
			for (int c = 0; c < layoutInfo.columnCount; c++) {
				remW -= layoutInfo.minColumnWidths[c];
				int dw = deltaW / (layoutInfo.columnCount - c);
				if (layoutInfo.minColumnWidths[c] + dw < 0) {
					dw = -layoutInfo.minColumnWidths[c];
				} else if (remW + deltaW < dw) {
					// Ensure there is enough width to take from in
					// the following columns.
					dw = remW + deltaW;
				}
				layoutInfo.minColumnWidths[c] += dw;
				deltaW -= dw;
			}
		}
		
		// Offset grid to the center of the viewport
		int xo = deltaW / 2;
		
		// Distribute the height weighted to all rows
		int remH = 0;
		float remWy = layoutInfo.rowWeight;
		for (int r = 0; remWy >= GSMathUtil.EPSILON_F && r < layoutInfo.rowCount; r++) {
			float wy = layoutInfo.rowWeights[r];
			int dh = (int)(deltaH * wy / remWy);
			// Ensure that we do not get negative height
			if (layoutInfo.minRowHeights[r] + dh < 0)
				dh = -layoutInfo.minRowHeights[r];
			layoutInfo.minRowHeights[r] += dh;
			// Accumulate the total height of the updated row heights
			remH += layoutInfo.minRowHeights[r];
			remWy -= wy;
			deltaH -= dh;
		}

		if (deltaH < 0) {
			// The weighted rows did not have enough height to give,
			// take it equally from the rows with no weight.
			for (int r = 0; r < layoutInfo.rowCount; r++) {
				remH -= layoutInfo.minRowHeights[r];
				int dh = deltaH / (layoutInfo.rowCount - r);
				if (layoutInfo.minRowHeights[r] + dh < 0) {
					dh = -layoutInfo.minRowHeights[r];
				} else if (remH + deltaH < dh) {
					// Ensure there is enough height to take from in
					// the following rows.
					dh = remH + deltaH;
				}
				layoutInfo.minRowHeights[r] += dh;
				deltaH -= dh;
			}
		}
		
		// Offset grid to the center of the viewport
		int yo = deltaH / 2;
		
		// Calculate x- and y-offsets of columns and rows
		int[] columnXs = new int[layoutInfo.columnCount];
		int[] rowYs = new int[layoutInfo.rowCount];
		
		for (int c = 0; c < layoutInfo.columnCount; c++) {
			columnXs[c] = xo;
			xo += layoutInfo.minColumnWidths[c];
		}
		for (int r = 0; r < layoutInfo.rowCount; r++) {
			rowYs[r] = yo;
			yo += layoutInfo.minRowHeights[r];
		}
		
		// Finally layout the components according to cell sizes
		GSRectangle displayBounds = new GSRectangle();
		
		for (GSPanel child : children) {
			GSLayout layout = child.getLayout();
			
			int gx = layout.get(GRID_X);
			int gy = layout.get(GRID_Y);
			int gw = layout.get(GRID_WIDTH);
			int gh = layout.get(GRID_HEIGHT);
			
			// Calculate display bounds from grid
			displayBounds.x = columnXs[gx];
			displayBounds.y = rowYs[gy];
			
			displayBounds.width = 0;
			for (int c = gx; c < gx + gw; c++)
				displayBounds.width += layoutInfo.minColumnWidths[c];
			displayBounds.height = 0;
			for (int r = gy; r < gy + gh; r++)
				displayBounds.height += layoutInfo.minRowHeights[r];
			
			// Layout panel inside of available display bounds
			layoutChild(child, displayBounds);
		}
	}
	
	private void layoutChild(GSPanel child, GSRectangle displayBounds) {
		GSLayout layout = child.getLayout();

		// Correct display bounds according to margin
		GSMargin margin = layout.get(MARGIN);
		displayBounds.x += Math.min(margin.left, displayBounds.width);
		displayBounds.y += Math.min(margin.top, displayBounds.height);
		displayBounds.width  -= margin.left + margin.right;
		displayBounds.height -= margin.top + margin.bottom;

		if (displayBounds.width <= 0 || displayBounds.height <= 0) {
			displayBounds.width = displayBounds.height = 0;
		} else {
			int remW = 0, remH = 0;
			
			// Correct width and height according to fill
			GSEFill fill = layout.get(FILL);
			if (fill != GSEFill.BOTH && fill != GSEFill.HORIZONTAL) {
				int prefW = layout.get(PREFERRED_WIDTH);
				remW = Math.max(displayBounds.width - prefW, 0);
				displayBounds.width -= remW;
			}
			
			if (fill != GSEFill.BOTH && fill != GSEFill.VERTICAL) {
				int prefH = layout.get(PREFERRED_HEIGHT);
				remH = Math.max(displayBounds.height - prefH, 0);
				displayBounds.height -= remH;
			}
		
			// Correct x-offset according to anchor
			if (remW != 0) {
				switch (layout.get(ANCHOR)) {
				case CENTER:
				case NORTH:
				case SOUTH:
				default:
					displayBounds.x += remW / 2;
					break;
				case NORTHEAST:
				case EAST:
				case SOUTHEAST:
					displayBounds.x += remW;
					break;
				case SOUTHWEST:
				case WEST:
				case NORTHWEST:
					break;
				}
			}
	
			// Correct y-offset according to anchor
			if (remH != 0) {
				switch (layout.get(ANCHOR)) {
				case CENTER:
				case EAST:
				case WEST:
				default:
					displayBounds.y += remH / 2;
					break;
				case SOUTHEAST:
				case SOUTH:
				case SOUTHWEST:
					displayBounds.y += remH;
					break;
				case NORTH:
				case NORTHEAST:
				case NORTHWEST:
					break;
				}
			}
		}
		
		// Finally, set the bounds of the child
		child.setBounds(displayBounds);
	}

	private GSGridLayoutInfo calculateLayoutInfo(GSParentPanel parent, GSILayoutProperty<GSDimension> sizeProperty) {
		GSGridLayoutInfo layoutInfo = initLayoutInfo(parent);
	
		calculateWeights(parent, layoutInfo);
		calculateGridSizes(parent, sizeProperty, layoutInfo);

		return layoutInfo;
	}
	
	private GSGridLayoutInfo initLayoutInfo(GSParentPanel parent) {
		// Pass 1, calculate number of columns and rows.
		int columnCount = 0, rowCount = 0;
		for (GSPanel child : parent.getChildren()) {
			GSLayout layout = child.getLayout();
			
			int columnEnd = layout.get(GRID_X) + layout.get(GRID_WIDTH);
			if (columnEnd > columnCount)
				columnCount = columnEnd;
			int rowEnd = layout.get(GRID_Y) + layout.get(GRID_HEIGHT);
			if (rowEnd > rowCount)
				rowCount = rowEnd;
		}
		
		return new GSGridLayoutInfo(columnCount, rowCount);
	}
	
	private void calculateWeights(GSParentPanel parent, GSGridLayoutInfo layoutInfo) {
		// Pass 2, calculate weights of columns and rows.
		for (GSPanel child : parent.getChildren()) {
			GSLayout layout = child.getLayout();
			
			int gx = layout.get(GRID_X);
			int gy = layout.get(GRID_Y);
			int gw = layout.get(GRID_WIDTH);
			int gh = layout.get(GRID_HEIGHT);

			// Calculate the average weight for each cell
			float avgWx = layout.get(WEIGHT_X) / gw;
			float avgWy = layout.get(WEIGHT_Y) / gh;
			
			for (int c = gx; c < gx + gw; c++) {
				if (avgWx > layoutInfo.columnWeights[c])
					layoutInfo.columnWeights[c] = avgWx;
			}
			
			for (int r = gy; r < gy + gh; r++) {
				if (avgWy > layoutInfo.rowWeights[r])
					layoutInfo.rowWeights[r] = avgWy;
			}
		}
		
		// Compute total column and row weights
		layoutInfo.columnWeight = 0.0f;
		for (int c = 0; c < layoutInfo.columnCount; c++)
			layoutInfo.columnWeight += layoutInfo.columnWeights[c];
		layoutInfo.rowWeight = 0.0f;
		for (int r = 0; r < layoutInfo.rowCount; r++)
			layoutInfo.rowWeight += layoutInfo.rowWeights[r];
	}
	
	private void calculateGridSizes(GSParentPanel parent, GSILayoutProperty<GSDimension> sizeProperty, GSGridLayoutInfo layoutInfo) {
		// Pass 3, distribute width and height to non-weighted cells
		for (GSPanel child : parent.getChildren()) {
			GSLayout layout = child.getLayout();

			// Get the minimum size of the panel
			GSDimension size = layout.get(sizeProperty);
			// Add margin, to get the required display size
			int w = size.getWidth()  + layout.get(LEFT_MARGIN) + layout.get(RIGHT_MARGIN);
			int h = size.getHeight() + layout.get(TOP_MARGIN)  + layout.get(BOTTOM_MARGIN);
			
			// Distribute the minimum width over each column
			int gx = layout.get(GRID_X);
			int gw = layout.get(GRID_WIDTH);

			float remWx = 0.0f;
			for (int c = gx; c < gx + gw; c++)
				remWx += layoutInfo.columnWeights[c];
			
			if (remWx < GSMathUtil.EPSILON_F) {
				// Distribute equally over columns
				int remW = w;
				for (int c = gx; c < gx + gw; c++) {
					int dw = remW / (gx + gw - c);
					if (dw > layoutInfo.minColumnWidths[c])
						layoutInfo.minColumnWidths[c] = dw;
					remW -= dw;
				}
			}

			// Distribute the minimum height over each row
			int gy = layout.get(GRID_Y);
			int gh = layout.get(GRID_HEIGHT);

			float remWy = 0.0f;
			for (int r = gy; r < gy + gh; r++)
				remWy += layoutInfo.rowWeights[r];

			if (remWy < GSMathUtil.EPSILON_F) {
				// Distribute equally over rows
				int remH = h;
				for (int r = gy; r < gy + gh; r++) {
					int dh = remH / (gy + gh - r);
					if (dh > layoutInfo.minRowHeights[r])
						layoutInfo.minRowHeights[r] = dh;
					remH -= dh;
				}
			}
		}
		
		// Pass 4, distribute the width and height to weighted cells
		for (GSPanel child : parent.getChildren()) {
			GSLayout layout = child.getLayout();

			// Get the minimum size of the panel
			GSDimension size = layout.get(sizeProperty);
			// Add margin, to get the required display size
			int w = size.getWidth()  + layout.get(LEFT_MARGIN) + layout.get(RIGHT_MARGIN);
			int h = size.getHeight() + layout.get(TOP_MARGIN)  + layout.get(BOTTOM_MARGIN);
			
			// Distribute the minimum width over each column
			int gx = layout.get(GRID_X);
			int gw = layout.get(GRID_WIDTH);

			int remW = w;
			float remWx = 0.0f;
			for (int c = gx; c < gx + gw; c++) {
				if (layoutInfo.columnWeights[c] < GSMathUtil.EPSILON_F) {
					// Subtract width from already distributed columns
					remW -= layoutInfo.minColumnWidths[c];
				} else {
					remWx += layoutInfo.columnWeights[c];
				}
			}
			
			if (remW > 0) {
				// Distribute weighted over columns
				for (int c = gx; remWx > 0.0f && c < gx + gw; c++) {
					float wx = layoutInfo.columnWeights[c];
					int dw = (int)(remW * wx / remWx);
					if (dw > layoutInfo.minColumnWidths[c])
						layoutInfo.minColumnWidths[c] = dw;
					remWx -= wx;
					remW -= dw;
				}
			}

			// Distribute the minimum height over each row
			int gy = layout.get(GRID_Y);
			int gh = layout.get(GRID_HEIGHT);

			int remH = h;
			float remWy = 0.0f;
			for (int r = gy; r < gy + gh; r++) {
				if (layoutInfo.rowWeights[r] < GSMathUtil.EPSILON_F) {
					// Subtract height from already distributed rows
					remH -= layoutInfo.minRowHeights[r];
				} else {
					remWy += layoutInfo.rowWeights[r];
				}
			}

			if (remH > 0) {
				// Distribute weighted over rows
				for (int r = gy; remWy > 0.0f && r < gy + gh; r++) {
					float wy = layoutInfo.rowWeights[r];
					int dh = (int)(remH * wy / remWy);
					if (dh > layoutInfo.minRowHeights[r])
						layoutInfo.minRowHeights[r] = dh;
					remWy -= wy;
					remH -= dh;
				}
			}
		}
	}
	
	private GSDimension getMinimumSize(GSGridLayoutInfo layoutInfo) {
		long wl = 0L, hl = 0L;
		
		for (int c = 0; c < layoutInfo.columnCount; c++)
			wl += layoutInfo.minColumnWidths[c];
		for (int r = 0; r < layoutInfo.rowCount; r++)
			hl += layoutInfo.minRowHeights[r];
	
		// Handle 32-bit integer overflow
		int w = (int)Math.min(wl, Integer.MAX_VALUE);
		int h = (int)Math.min(hl, Integer.MAX_VALUE);
		
		return new GSDimension(w, h);
	}
	
	private class GSGridLayoutInfo {
		
		private int columnCount;
		private int rowCount;
		
		private float[] columnWeights;
		private float[] rowWeights;
		private float columnWeight;
		private float rowWeight;
		
		private int[] minColumnWidths;
		private int[] minRowHeights;

		public GSGridLayoutInfo(int columnCount, int rowCount) {
			this.columnCount = columnCount;
			this.rowCount = rowCount;
			
			columnWeights = new float[columnCount];
			rowWeights = new float[rowCount];
			columnWeight = 0.0f;
			rowWeight = 0.0f;
			
			minColumnWidths = new int[columnCount];
			minRowHeights = new int[rowCount];
		}
	}
}
