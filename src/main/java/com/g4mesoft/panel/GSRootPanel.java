package com.g4mesoft.panel;

import com.g4mesoft.panel.event.GSEventDispatcher;

public final class GSRootPanel extends GSParentPanel {

	private GSPanel content;
	
	GSRootPanel() {
		content = null;
	}
	
	@Override
	protected void onBoundsChanged() {
		super.onBoundsChanged();

		updateContentBounds();
	}
	
	public void setContent(GSPanel panel) {
		if (content != null)
			remove(content);

		content = panel;
		
		if (panel != null) {
			updateContentBounds();
			
			add(panel);
			
			GSEventDispatcher eventDispatcher = GSPanelContext.getEventDispatcher();
			
			// Only request focus if panels have not requested
			// focus when they were added to the root panel.
			GSPanel focusedPanel = eventDispatcher.getFocusedPanel();
			if (focusedPanel == this || focusedPanel == null)
				panel.requestFocus();
		} else {
			requestFocus();
		}
	}
	
	private void updateContentBounds() {
		if (content != null)
			content.setBounds(0, 0, width, height);
	}
}
