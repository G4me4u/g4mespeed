package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSIRenderer2D;

public interface GSIBorder {

	public void render(GSIRenderer2D renderer, GSPanel panel);
	
	public GSSpacing getOuterSpacing(GSPanel panel);

	public boolean isFullyOpaque();
	
}
