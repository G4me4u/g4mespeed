package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSLineBorder implements GSIBorder {

	private final int color;
	
	private final int topThickness;
	private final int leftThickness;
	private final int bottomThickness;
	private final int rightThickness;

	public GSLineBorder(int color) {
		this(color, 1, 1, 1, 1);
	}

	public GSLineBorder(int color, int thickness) {
		this(color, thickness, thickness, thickness, thickness);
	}

	public GSLineBorder(int color, GSSpacing thickness) {
		this(color, thickness.top, thickness.left, thickness.bottom, thickness.right);
	}
	
	public GSLineBorder(int color, int topThickness, int leftThickness, int bottomThickness, int rightThickness) {
		if (topThickness < 0 || leftThickness < 0 || bottomThickness < 0 || rightThickness < 0)
			throw new IllegalArgumentException("Thickness must be non-negative!");
		
		this.color = color;
		
		this.topThickness = topThickness;
		this.leftThickness = leftThickness;
		this.bottomThickness = bottomThickness;
		this.rightThickness = rightThickness;
	}
	
	@Override
	public void render(GSIRenderer2D renderer, GSPanel panel) {
		// Render the border lines as follows:
		// -----------------
		// |       T       |
		// |---------------|
		// |   |       |   |
		// | L | INNER | R |
		// |   |       |   |
		// |---------------|
		// |       B       |
		// -----------------

		int outerWidth = panel.getOuterWidth();
		int outerHeight = panel.getOuterHeight();
		
		// Top and bottom always draw the entire panel outer width.
		if (topThickness != 0)
			renderer.fillRect(0, 0, outerWidth, topThickness, color);
		if (bottomThickness != 0) {
			int by = outerHeight - bottomThickness;
			renderer.fillRect(0, by, outerWidth, bottomThickness, color);
		}
		
		// Calculate height of left and right border lines.
		int lrh = Math.max(outerHeight - topThickness - bottomThickness, 0);

		if (leftThickness != 0)
			renderer.fillRect(0, topThickness, leftThickness, lrh, color);
		if (rightThickness != 0) {
			int rx = outerWidth - rightThickness;
			renderer.fillRect(rx, topThickness, rightThickness, lrh, color);
		}
	}
	
	@Override
	public GSSpacing getOuterSpacing(GSPanel panel) {
		return new GSSpacing(topThickness, leftThickness, bottomThickness, rightThickness);
	}
	
	@Override
	public boolean isFullyOpaque() {
		return true;
	}
}
