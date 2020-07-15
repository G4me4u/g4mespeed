package com.g4mesoft.gui;

import com.g4mesoft.gui.event.GSIButtonStroke;
import com.g4mesoft.gui.event.GSKeyButtonStroke;
import com.g4mesoft.gui.event.GSKeyEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

public class GSBasePanel extends GSParentPanel {

	private static final int BACKGROUND_TOP_COLOR    = 0xC0101010;
	private static final int BACKGROUND_BOTTOM_COLOR = 0xD0101010;
	
	private GSIButtonStroke closeButton;
	
	public GSBasePanel() {
		closeButton = new GSKeyButtonStroke(GSKeyEvent.KEY_ESCAPE);
		
		putButtonStroke(closeButton, this::closePanel);
	}
	
	public GSIButtonStroke getCloseButton() {
		return closeButton;
	}

	public void setCloseButton(GSIButtonStroke closeButton) {
		if (closeButton == null)
			throw new IllegalArgumentException("closeButtonStroke is null!");
		
		removeButtonStroke(this.closeButton);
		
		this.closeButton = closeButton;
		
		putButtonStroke(closeButton, this::closePanel);
	}
	
	protected void closePanel() {
		GSElementContext.setContent(null);
	}
	
	protected void renderBackground(GSIRenderer2D renderer) {
		renderer.fillRectGradient(0, 0, width, height, BACKGROUND_TOP_COLOR, BACKGROUND_BOTTOM_COLOR);
	}
}
