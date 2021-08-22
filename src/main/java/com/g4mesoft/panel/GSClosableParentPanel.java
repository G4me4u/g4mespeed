package com.g4mesoft.panel;

import com.g4mesoft.panel.event.GSIButtonStroke;
import com.g4mesoft.panel.event.GSKeyButtonStroke;
import com.g4mesoft.panel.event.GSKeyEvent;

public class GSClosableParentPanel extends GSParentPanel implements GSIClosable {

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
		GSPanelContext.openContent(null);
	}
}
