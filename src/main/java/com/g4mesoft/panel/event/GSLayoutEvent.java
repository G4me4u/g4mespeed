package com.g4mesoft.panel.event;

public final class GSLayoutEvent extends GSEvent {

	public static final int ADDED_TYPE   = 400;
	public static final int REMOVED_TYPE = 401;
	public static final int RESIZED_TYPE = 402;
	public static final int MOVED_TYPE   = 403;
	public static final int SHOWN_TYPE   = 404;
	public static final int HIDDEN_TYPE  = 405;
	
	public static final int FIRST_TYPE = ADDED_TYPE;
	public static final int LAST_TYPE  = HIDDEN_TYPE;
	
	private final int type;
	
	public GSLayoutEvent(int type) {
		if (type < FIRST_TYPE || type > LAST_TYPE)
			type = UNKNOWN_TYPE;
		
		this.type = type;
	}
	
	@Override
	public int getType() {
		return type;
	}
	
	public static GSLayoutEvent createAddedEvent() {
		return new GSLayoutEvent(ADDED_TYPE);
	}

	public static GSLayoutEvent createRemovedEvent() {
		return new GSLayoutEvent(REMOVED_TYPE);
	}
	
	public static GSLayoutEvent createResizedEvent() {
		return new GSLayoutEvent(RESIZED_TYPE);
	}

	public static GSLayoutEvent createMovedEvent() {
		return new GSLayoutEvent(MOVED_TYPE);
	}

	public static GSLayoutEvent createShownEvent() {
		return new GSLayoutEvent(SHOWN_TYPE);
	}

	public static GSLayoutEvent createHiddenEvent() {
		return new GSLayoutEvent(HIDDEN_TYPE);
	}
}
