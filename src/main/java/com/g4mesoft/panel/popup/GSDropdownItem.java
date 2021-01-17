package com.g4mesoft.panel.popup;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSPanel;

public abstract class GSDropdownItem extends GSPanel {

	protected static final int ICON_SIZE        = 10;
	protected static final int ICON_MARGIN      = 4;
	protected static final int PADDING          = 2;
	
	protected static final int BACKGROUND_COLOR         = GSDropdown.BACKGROUND_COLOR;
	protected static final int TEXT_COLOR               = 0xFFCCCCCC;
	protected static final int HOVERED_BACKGROUND_COLOR = 0xFF094771;
	protected static final int HOVERED_TEXT_COLOR       = 0xFFF3F6F8;
	
	@Override
	public abstract GSDimension calculatePreferredSize();
	
	@Override
	public GSECursorType getCursor() {
		return GSECursorType.HAND;
	}
}
