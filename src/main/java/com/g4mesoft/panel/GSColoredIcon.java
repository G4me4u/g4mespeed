package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSColoredIcon extends GSIcon {

	private final int color;
	private final int width;
	private final int height;

	public GSColoredIcon(int color, int width, int height) {
		this.color = color;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void render(GSIRenderer2D renderer, GSRectangle bounds) {
		int w = Math.min(width, bounds.width);
		int h = Math.min(height, bounds.height);
		renderer.fillRect(bounds.x, bounds.y, w, h, color);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
