package com.g4mesoft.panel.popup;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.GSRootPanel;

public class GSPopup extends GSParentPanel {

	private final GSPanel content;
	
	public GSPopup(GSPanel content) {
		if (content == null)
			throw new IllegalArgumentException("content is null");
		this.content = content;

		super.add(content);
	}
	
	@Override
	public void add(GSPanel panel) {
		throw new UnsupportedOperationException("Popups can only have one child");
	}

	@Override
	public void remove(GSPanel panel) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void onBoundsChanged() {
		super.onBoundsChanged();

		content.setBounds(0, 0, width, height);
	}
	
	@Override
	public GSDimension calculateMinimumSize() {
		return content.getMinimumSize();
	}

	@Override
	public GSDimension calculatePreferredSize() {
		return content.getPreferredSize();
	}
	
	public void show(int x, int y) {
		if (getParent() != null)
			return;
		
		GSDimension pref = getPreferredSize();
		setBounds(x, y, pref.getWidth(), pref.getHeight());
	
		GSRootPanel rootPanel = GSPanelContext.getRootPanel();
		rootPanel.add(this, GSRootPanel.POPUP_LAYER);
	}
	
	public void hide() {
		GSPanel parent = getParent();
		if (parent != null)
			parent.remove(this);
	}
}
