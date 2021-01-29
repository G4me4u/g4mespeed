package com.g4mesoft.panel;

import com.g4mesoft.panel.event.GSIButtonStroke;
import com.g4mesoft.panel.event.GSKeyButtonStroke;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSClosableParentPanel extends GSParentPanel implements GSIClosable {

	private static final int BACKGROUND_TOP_COLOR    = 0xC0101010;
	private static final int BACKGROUND_BOTTOM_COLOR = 0xD0101010;
	
	private GSIButtonStroke closeButton;
	
	public GSClosableParentPanel() {
		setCloseButton(new GSKeyButtonStroke(GSKeyEvent.KEY_ESCAPE));
	}
	
	public GSIButtonStroke getCloseButton() {
		return closeButton;
	}

	public void setCloseButton(GSIButtonStroke closeButton) {
		if (closeButton == null)
			throw new IllegalArgumentException("closeButtonStroke is null!");
		
		if (this.closeButton != null)
			removeButtonStroke(this.closeButton);
		
		this.closeButton = closeButton;
		
		putButtonStroke(closeButton, this::close);
	}
	
	@Override
	public void close() {
		GSPanelContext.setContent(null);
	}
	
	protected void renderBackground(GSIRenderer2D renderer) {
		renderer.fillVGradient(0, 0, width, height, BACKGROUND_TOP_COLOR, BACKGROUND_BOTTOM_COLOR);
	}
}
