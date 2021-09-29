package com.g4mesoft.panel.scroll;

import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;

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
	protected void validate() {
		super.validate();
	
		// Sometimes, specifically when the viewport is added to a
		// scroll panel, layout causes the viewport to invalidate.
		if (invalidateLater) {
			// Attempt to solve the issue by forcing invalidate and
			// validating immediately.
			forcedInvalidate();
			super.validate();
			// No need to invalidate later
			invalidateLater = false;
		}
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
