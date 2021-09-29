package com.g4mesoft.panel.dropdown;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSDropdownSeparator extends GSDropdownItem {

	private static final int SEPARATOR_COLOR = 0xFF616162;
	
	@Override
	protected void renderForeground(GSIRenderer2D renderer) {
		super.renderForeground(renderer);
	
		renderer.drawHLine(0, innerWidth, innerHeight / 2, SEPARATOR_COLOR);
	}
	
	@Override
	protected GSDimension calculatePreferredInnerSize() {
		return new GSDimension(0, 2 * PADDING + 1);
	}
}
