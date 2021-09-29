package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSEmptyBorder implements GSIBorder {

	private final int top;
	private final int left;
	private final int bottom;
	private final int right;

	public GSEmptyBorder(GSSpacing spacing) {
		this(spacing.top, spacing.left, spacing.bottom, spacing.right);
	}

	public GSEmptyBorder(int spacing) {
		this(spacing, spacing, spacing, spacing);
	}
	
	public GSEmptyBorder(int top, int left, int bottom, int right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}
	
	@Override
	public void render(GSIRenderer2D renderer, GSPanel panel) {
		// Do nothing.
	}
	
	@Override
	public GSSpacing getOuterSpacing(GSPanel panel) {
		return new GSSpacing(top, left, bottom, right);
	}
	
	@Override
	public boolean isFullyOpaque() {
		return false;
	}
}
