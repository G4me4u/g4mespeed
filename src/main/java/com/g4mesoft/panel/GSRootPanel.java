package com.g4mesoft.panel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.panel.event.GSEventDispatcher;

public final class GSRootPanel extends GSParentPanel {

	public static final int DEFAULT_LAYER = 0;
	public static final int POPUP_LAYER = 10;
	
	private GSPanel content;
	
	private Map<GSPanel, Integer> layers;
	
	GSRootPanel() {
		content = null;
		
		layers = new HashMap<>();
	}
	
	@Override
	protected void layout() {
		if (content != null)
			content.setOuterBounds(0, 0, innerWidth, innerHeight);
	}
	
	@Override
	public void add(GSPanel panel) {
		add(panel, DEFAULT_LAYER);
	}
	
	public void add(GSPanel panel, int layer) {
		super.add(panel);

		layers.put(panel, layer);
		
		Collections.sort(children, this::compareLayers);
	}
	
	private int compareLayers(GSPanel p0, GSPanel p1) {
		int l0 = layers.getOrDefault(p0, DEFAULT_LAYER);
		int l1 = layers.getOrDefault(p1, DEFAULT_LAYER);
		// Sort layers in increasing order.
		return Integer.compare(l0, l1);
	}
	
	@Override
	protected void onChildRemoved(GSPanel child) {
		super.onChildRemoved(child);

		layers.remove(child);
	}
	
	public void setContent(GSPanel panel) {
		if (content != null)
			remove(content);

		content = panel;
		
		if (panel != null) {
			add(panel, DEFAULT_LAYER);
			
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
}
