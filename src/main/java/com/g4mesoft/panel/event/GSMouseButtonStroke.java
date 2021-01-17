package com.g4mesoft.panel.event;

public class GSMouseButtonStroke implements GSIButtonStroke {

	private final int button;
	private final int modifiers;

	public GSMouseButtonStroke(int button) {
		this(button, GSMouseEvent.NO_MODIFIERS);
	}
	
	public GSMouseButtonStroke(int button, int modifiers) {
		this.button = button;
		this.modifiers = modifiers;
	}
	
	@Override
	public boolean isMatching(GSEvent event) {
		if (!(event instanceof GSMouseEvent))
			return false;
		
		GSMouseEvent me = (GSMouseEvent)event;
		if (me.getType() != GSMouseEvent.MOUSE_DRAGGED_TYPE &&
		    me.getType() != GSMouseEvent.MOUSE_PRESSED_TYPE &&
		    me.getType() != GSMouseEvent.MOUSE_RELEASED_TYPE) {
			
			// Only support pressed and released.
			return false;
		}
		
		return (me.getButton() == button && me.isModifierHeld(modifiers));
	}
}
