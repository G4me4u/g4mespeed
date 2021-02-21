package com.g4mesoft.panel.dropdown;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSDropdownSeparator extends GSDropdownItem {

	private static final int SEPARATOR_COLOR = 0xFF616162;
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);
	
		renderer.drawHLine(0, width, height / 2, SEPARATOR_COLOR);
	}
	
	@Override
	public GSDimension calculatePreferredSize() {
		return new GSDimension(0, 2 * PADDING + 1);
	}
}