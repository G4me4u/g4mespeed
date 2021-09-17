package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSScrollPanelCorner extends GSPanel {

	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF000000;
	
	private int backgroundColor;
	
	public GSScrollPanelCorner() {
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		if (((backgroundColor >> 24) & 0xFF) != 0)
			renderer.fillRect(0, 0, width, height, backgroundColor);

		super.render(renderer);
	}
	
	public int getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
}
