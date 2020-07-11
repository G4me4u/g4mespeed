package com.g4mesoft.gui.event;

public class GSKeyButtonStroke implements GSIButtonStroke {

	private final int keyCode;
	private final int modifiers;

	public GSKeyButtonStroke(int keyCode) {
		this(keyCode, GSKeyEvent.NO_MODIFIERS);
	}

	public GSKeyButtonStroke(int keyCode, int modifiers) {
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}
	
	@Override
	public boolean isMatching(GSEvent event) {
		if (!(event instanceof GSKeyEvent))
			return false;
		
		GSKeyEvent ke = (GSKeyEvent)event;
		if (ke.getType() != GSKeyEvent.KEY_PRESSED_TYPE &&
		    ke.getType() != GSKeyEvent.KEY_REPEATED_TYPE &&
		    ke.getType() != GSKeyEvent.KEY_RELEASED_TYPE) {
			
			// Only support pressed, repeat and released events.
			return false;
		}
		
		return (ke.getKeyCode() == keyCode && ke.isModifierHeld(modifiers));
	}
}
