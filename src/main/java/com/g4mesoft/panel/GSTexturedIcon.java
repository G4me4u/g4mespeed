package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.renderer.GSITextureRegion;

public class GSTexturedIcon extends GSIcon {

	private final GSITextureRegion texture;
	
	public GSTexturedIcon(GSITextureRegion texture) {
		this.texture = texture;
	}
	
	@Override
	public void render(GSIRenderer2D renderer, GSRectangle bounds) {
		renderer.pushClip(bounds.x, bounds.y, bounds.width, bounds.height);
		
		// Draw the texture in the center of the allowed area
		int tx = bounds.x + (bounds.width  - getWidth() ) / 2;
		int ty = bounds.y + (bounds.height - getHeight()) / 2;
		renderer.drawTexture(texture, tx, ty);
		
		renderer.popClip();
	}

	@Override
	public int getWidth() {
		return texture.getRegionWidth();
	}

	@Override
	public int getHeight() {
		return texture.getRegionHeight();
	}
}
