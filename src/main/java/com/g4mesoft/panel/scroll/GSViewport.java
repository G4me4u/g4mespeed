package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSViewport extends GSParentPanel {

	private int offsetX;
	private int offsetY;
	
	public GSViewport() {
		setFocusable(false);
		setLayoutManager(new GSViewportLayoutManager());
	}
	
	@Override
	public void add(GSPanel panel) {
		setContent(panel);
	}

	public GSPanel getContent() {
		return isEmpty() ? null : get(0);
	}
	
	public void setContent(GSPanel panel) {
		if (!isEmpty()) {
			// Ensure that we have at most one child at
			// any point in time.
			removeAll();
		}
		
		if (panel != null)
			super.add(panel);
	}
	
	@Override
	public void preRender(GSIRenderer2D renderer) {
		renderer.pushClip(x, y, width, height);
		super.preRender(renderer);
	}
	
	@Override
	public void postRender(GSIRenderer2D renderer) {
		super.postRender(renderer);
		renderer.popClip();
	}
	
	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}
	
	/* Visible for GSScrollPanel */
	void setOffset(int offsetX, int offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		
		if (!isEmpty()) {
			// Offset has changed, and the viewport must be
			// revalidated by the layout manager.
			invalidate();
		}
	}
}
