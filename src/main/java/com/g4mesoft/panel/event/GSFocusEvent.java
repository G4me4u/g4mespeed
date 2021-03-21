package com.g4mesoft.panel.event;

public final class GSFocusEvent extends GSEvent {

	public static final int FOCUS_GAINED_TYPE  = 300;
	public static final int FOCUS_LOST_TYPE    = 301;
	
	public static final int FIRST_TYPE = FOCUS_GAINED_TYPE;
	public static final int LAST_TYPE  = FOCUS_LOST_TYPE;
	
	private final int type;
	
	public GSFocusEvent(int type) {
		if (type < FIRST_TYPE || type > LAST_TYPE)
			type = UNKNOWN_TYPE;
		
		this.type = type;
	}
	
	@Override
	public int getType() {
		return type;
	}
	
	public static GSFocusEvent createFocusGainedEvent() {
		return new GSFocusEvent(FOCUS_GAINED_TYPE);
	}

	public static GSFocusEvent createFocusLostEvent() {
		return new GSFocusEvent(FOCUS_LOST_TYPE);
	}
}
