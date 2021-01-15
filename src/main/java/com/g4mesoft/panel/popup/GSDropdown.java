package com.g4mesoft.panel.popup;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSDropdown extends GSParentPanel {

	protected static final int BACKGROUND_COLOR = 0xFF252526;
	protected static final int VERTICAL_PADDING = 4;
	
	@Override
	public void add(GSPanel panel) {
		// Illegal method call to add. Use GSDropdownMenu#addItem instead.
		throw new UnsupportedOperationException("Drop-down menus only support drop-down items.");
	}
	
	public void addItem(GSDropdownItem item) {
		super.add(item);
		
		if (isVisible()) {
			// Generally it is a bad idea to modify the menu
			// when it is visible. In case it is required, we
			// have to update the menu layout.
			layoutMenuItems();
		}
	}
	
	@Override
	protected void onBoundsChanged() {
		super.onBoundsChanged();

		layoutMenuItems();
	}
	
	private void layoutMenuItems() {
		int y = VERTICAL_PADDING;
		
		for (GSPanel child : getChildren()) {
			// Since children must implement GSDropdownItem, it is
			// assumed that they have a preferred size.
			GSDimension pref = child.getPreferredSize();
			// Ensure there is actually space for the preferred size.
			int h = Math.max(Math.min(pref.getHeight(), height - y), 0);
			child.setBounds(0, y, width, h);

			y += h;
		}
	}

	@Override
	public void render(GSIRenderer2D renderer) {
		renderer.fillRect(0, 0, width, height, BACKGROUND_COLOR);

		super.render(renderer);
	}
	
	@Override
	public GSDimension calculateMinimumSize() {
		int mnw = 0;
		int mnh = 0;
		
		for (GSPanel child : getChildren()) {
			GSDimension mn = child.getMinimumSize();
			if (mn.getWidth() > mnw)
				mnw = mn.getWidth();
			mnh += mn.getHeight();
		}
		
		return new GSDimension(mnw, mnh + 2 * VERTICAL_PADDING);
	}
	
	@Override
	public GSDimension calculatePreferredSize() {
		int pw = 0;
		int ph = 0;
		
		for (GSPanel child : getChildren()) {
			GSDimension pref = child.getPreferredSize();
			if (pref.getWidth() > pw)
				pw = pref.getWidth();
			ph += pref.getHeight();
		}
		
		return new GSDimension(pw, ph + 2 * VERTICAL_PADDING);
	}
}
