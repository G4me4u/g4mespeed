package com.g4mesoft.panel.button;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSSelectedIcon extends GSRadioButtonIcon {

	private static final int FILL_INDENT = 2;
	
	public GSSelectedIcon(int color, int preferredSize) {
		super(color, preferredSize);
	}
	
	@Override
	public void renderIcon(GSIRenderer2D renderer, int x, int y, int size) {
		renderer.drawRect(x, y, size, size, color);
		
		// Draw selection fill
		size -= 2 * FILL_INDENT;
		if (size > 0)
			renderer.fillRect(x + FILL_INDENT, y + FILL_INDENT, size, size, color);
	}
}
