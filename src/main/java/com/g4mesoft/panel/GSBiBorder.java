package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSIRenderer2D;

public class GSBiBorder implements GSIBorder {

	private final GSIBorder insideBorder;
	private final GSIBorder outsideBorder;
	
	public GSBiBorder(GSIBorder insideBorder, GSIBorder outsideBorder) {
		if (insideBorder == null)
			throw new IllegalArgumentException("insideBorder is null");
		if (outsideBorder == null)
			throw new IllegalArgumentException("outsideBorder is null");
		
		this.insideBorder = insideBorder;
		this.outsideBorder = outsideBorder;
	}
	
	@Override
	public void render(GSIRenderer2D renderer, GSPanel panel) {
		renderer.pushMatrix();
		
		outsideBorder.render(renderer, panel);
		GSSpacing outside = outsideBorder.getOuterSpacing(panel);
		renderer.translate(outside.left, outside.top);
		
		insideBorder.render(renderer, panel);
		
		renderer.popMatrix();
	}

	@Override
	public GSSpacing getOuterSpacing(GSPanel panel) {
		GSSpacing inside = insideBorder.getOuterSpacing(panel);
		GSSpacing outside = outsideBorder.getOuterSpacing(panel);
		return new GSSpacing(inside.top    + outside.top,
		                     inside.left   + outside.left,
		                     inside.bottom + outside.bottom,
		                     inside.right  + outside.right);
	}
	
	@Override
	public boolean isFullyOpaque() {
		return insideBorder.isFullyOpaque() && outsideBorder.isFullyOpaque();
	}
}
