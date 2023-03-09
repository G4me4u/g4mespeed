package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSScrollPanelCorner extends GSPanel {

	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF060606;
	
	private int backgroundColor;
	
	public GSScrollPanelCorner() {
		this(DEFAULT_BACKGROUND_COLOR);
		
		super.setFocusable(false);
	}

	public GSScrollPanelCorner(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		if (((backgroundColor >> 24) & 0xFF) != 0)
			renderer.fillRect(0, 0, width, height, backgroundColor);

		super.render(renderer);
	}

	@Override
	public final void setFocusable(boolean focusable) {
		// Do nothing
	}
	
	public int getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
