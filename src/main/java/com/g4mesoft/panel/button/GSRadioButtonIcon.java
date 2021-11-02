package com.g4mesoft.panel.button;

import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.renderer.GSIRenderer2D;

public abstract class GSRadioButtonIcon extends GSIcon {

	protected final int color;
	protected final int preferredSize;
	
	public GSRadioButtonIcon(int color, int preferredSize) {
		this.color = color;
		this.preferredSize = preferredSize;
	}
	
	@Override
	public void render(GSIRenderer2D renderer, GSRectangle bounds) {
		int s = Math.min(bounds.width, bounds.height);
		int x = bounds.x + (bounds.width  - s) / 2;
		int y = bounds.y + (bounds.height - s) / 2;
		
		renderIcon(renderer, x, y, s);
	}
	
	protected abstract void renderIcon(GSIRenderer2D renderer, int x, int y, int size);

	@Override
	public int getWidth() {
		return preferredSize;
	}

	@Override
	public int getHeight() {
		return preferredSize;
	}
	
	public int getColor() {
		return color;
	}
}
