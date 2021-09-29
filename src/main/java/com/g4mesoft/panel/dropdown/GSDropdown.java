package com.g4mesoft.panel.dropdown;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSILayoutProperty;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.GSPopup;
import com.g4mesoft.panel.event.GSFocusEvent;
import com.g4mesoft.panel.event.GSIFocusEventListener;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSKeyEvent;

public class GSDropdown extends GSParentPanel implements GSIKeyListener, GSIFocusEventListener {

	protected static final int DEFAULT_BACKGROUND_COLOR = 0xFF252526;
	protected static final int VERTICAL_PADDING = 4;
	
	private final List<GSIActionListener> actionListeners;
	
	private boolean separateNextItem;
	
	public GSDropdown() {
		actionListeners = new ArrayList<>();
		
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
		
		addKeyEventListener(this);
		addFocusEventListener(this);
	}
	
	@Override
	public void add(GSPanel panel) {
		if (!(panel instanceof GSDropdownItem))
			throw new IllegalArgumentException("Drop-down menus only support drop-down items.");
		addItem((GSDropdownItem)panel);
	}
	
	public void addItem(GSDropdownItem item) {
		if (separateNextItem) {
			separateNextItem = false;
			super.add(new GSDropdownSeparator());
		}
		
		super.add(item);
	}
	
	public void separate() {
		separateNextItem |= !isEmpty();
	}
	
	@Override
	public void layout() {
		int y = VERTICAL_PADDING;
		
		for (GSPanel child : getChildren()) {
			// Since children must implement GSDropdownItem, it is
			// assumed that they have a preferred size.
			GSDimension pref = child.getProperty(PREFERRED_SIZE);
			// Ensure there is actually space for the preferred size.
			int h = Math.max(Math.min(pref.getHeight(), innerHeight - y), 0);
			child.setOuterBounds(0, y, innerWidth, h);

			y += h;
		}
	}
	
	@Override
	public GSDimension calculateMinimumInnerSize() {
		return calculateSize(MINIMUM_SIZE);
	}
	
	@Override
	protected GSDimension calculatePreferredInnerSize() {
		return calculateSize(PREFERRED_SIZE);
	}
	
	private GSDimension calculateSize(GSILayoutProperty<GSDimension> sizeProperty) {
		int w = 0, h = 0;
		
		for (GSPanel child : getChildren()) {
			GSDimension size = child.getProperty(sizeProperty);
			if (size.getWidth() > w)
				w = size.getWidth();
			h += size.getHeight();
		}
		
		return new GSDimension(w, h + 2 * VERTICAL_PADDING);
	}
	
	public boolean isEmpty() {
		return children.isEmpty();
	}
	
	/* Visible for GSDropdownSubMenu */
	void addActionListener(GSIActionListener listener) {
		actionListeners.add(listener);
	}

	/* Visible for GSDropdownSubMenu */
	void removeActionListener(GSIActionListener listener) {
		actionListeners.remove(listener);
	}
	
	/* Visible for GSDropdownAction */
	void onActionPerformed() {
		hide();
	}
	
	private void hide() {
		GSPanel parent = getParent();
		if (parent instanceof GSPopup)
			((GSPopup)parent).hide();

		actionListeners.forEach(GSIActionListener::actionPerformed);
	}

	@Override
	public void keyPressed(GSKeyEvent event) {
		switch (event.getKeyCode()) {
		case GSKeyEvent.KEY_ESCAPE:
		case GSKeyEvent.KEY_ENTER:
			hide();
			event.consume();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void focusLost(GSFocusEvent event) {
		if (!hasPopupVisible()) {
			hide();
			event.consume();
		}
	}
}
