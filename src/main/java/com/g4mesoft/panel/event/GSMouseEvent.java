package com.g4mesoft.panel.event;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.panel.GSLocation;

public final class GSMouseEvent extends GSEvent {

	public static final int MOUSE_MOVED_TYPE    = 100;
	public static final int MOUSE_DRAGGED_TYPE  = 101;
	
	public static final int MOUSE_PRESSED_TYPE  = 102;
	public static final int MOUSE_RELEASED_TYPE = 103;
	
	public static final int MOUSE_SCROLLED_TYPE = 104;
	
	public static final int FIRST_TYPE = MOUSE_MOVED_TYPE;
	public static final int LAST_TYPE  = MOUSE_SCROLLED_TYPE;
	
	public static final int UNKNOWN_BUTTON = -1;
	
	public static final int BUTTON_1 = GLFW.GLFW_MOUSE_BUTTON_1;
	public static final int BUTTON_2 = GLFW.GLFW_MOUSE_BUTTON_2;
	public static final int BUTTON_3 = GLFW.GLFW_MOUSE_BUTTON_3;
	public static final int BUTTON_4 = GLFW.GLFW_MOUSE_BUTTON_4;
	public static final int BUTTON_5 = GLFW.GLFW_MOUSE_BUTTON_5;
	public static final int BUTTON_6 = GLFW.GLFW_MOUSE_BUTTON_6;
	public static final int BUTTON_7 = GLFW.GLFW_MOUSE_BUTTON_7;
	public static final int BUTTON_8 = GLFW.GLFW_MOUSE_BUTTON_8;

	public static final int BUTTON_LEFT   = GLFW.GLFW_MOUSE_BUTTON_LEFT;
	public static final int BUTTON_MIDDLE = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
	public static final int BUTTON_RIGHT  = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
	
	private static final int FIRST_BUTTON = GLFW.GLFW_MOUSE_BUTTON_1;
	private static final int LAST_BUTTON  = GLFW.GLFW_MOUSE_BUTTON_8;
	
	/* The type of mouse event */
	private final int type;
	
	/* The current mouse coordinates */
	private int x;
	private int y;
	
	/* The button that is held, pressed, released, or unknown button */
	private final int button;
	
	/* The modifiers that are held when pressing or releasing */
	private final int modifiers;
	
	/* DragX and DragY when dragging, ScrollX and ScrollY when scrolling */
	private final float extraX;
	private final float extraY;
	
	public GSMouseEvent(int type, int x, int y, int button, int modifiers, float extraX, float extraY) {
		if (type < FIRST_TYPE || type > LAST_TYPE)
			type = UNKNOWN_TYPE;
		if (button < FIRST_BUTTON || button > LAST_BUTTON)
			button = UNKNOWN_BUTTON;
		
		this.type = type;
		
		this.x = x;
		this.y = y;
		
		this.button = button;
		this.modifiers = modifiers & ALL_MODIFIERS;
		
		this.extraX = extraX;
		this.extraY = extraY;
	}
	
	@Override
	public int getType() {
		return type;
	}
	
	public int getX() {
		return x;
	}

	/* Visible for GSEventDispatcher */
	void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	/* Visible for GSEventDispatcher */
	void setY(int y) {
		this.y = y;
	}
	
	public GSLocation getLocation() {
		return new GSLocation(x, y);
	}

	/* Visible for GSEventDispatcher */
	void setLocation(GSLocation location) {
		setX(location.getX());
		setY(location.getY());
	}
	
	public int getButton() {
		return button;
	}
	
	public int getModifiers() {
		return modifiers;
	}

	public boolean isModifierHeld(int modifier) {
		return (modifiers & modifier) == modifier;
	}
	
	public float getDragX() {
		return (type == MOUSE_DRAGGED_TYPE) ? extraX : 0.0f;
	}

	public float getDragY() {
		return (type == MOUSE_DRAGGED_TYPE) ? extraY : 0.0f;
	}

	public float getScrollX() {
		return (type == MOUSE_SCROLLED_TYPE) ? extraX : 0.0f;
	}

	public float getScrollY() {
		return (type == MOUSE_SCROLLED_TYPE) ? extraY : 0.0f;
	}
	
	public static GSMouseEvent createMouseMovedEvent(int x, int y) {
		return new GSMouseEvent(MOUSE_MOVED_TYPE, x, y, UNKNOWN_BUTTON, NO_MODIFIERS, 0.0f, 0.0f);
	}

	public static GSMouseEvent createMouseDraggedEvent(int x, int y, int button, float dragX, float dragY) {
		return new GSMouseEvent(MOUSE_DRAGGED_TYPE, x, y, button, NO_MODIFIERS, dragX, dragY);
	}

	public static GSMouseEvent createMousePressedEvent(int x, int y, int button, int modifiers) {
		return new GSMouseEvent(MOUSE_PRESSED_TYPE, x, y, button, modifiers, 0.0f, 0.0f);
	}

	public static GSMouseEvent createMouseReleasedEvent(int x, int y, int button, int modifiers) {
		return new GSMouseEvent(MOUSE_RELEASED_TYPE, x, y, button, modifiers, 0.0f, 0.0f);
	}

	public static GSMouseEvent createMouseScrolledEvent(int x, int y, float scrollX, float scrollY) {
		return new GSMouseEvent(MOUSE_SCROLLED_TYPE, x, y, UNKNOWN_BUTTON, NO_MODIFIERS, scrollX, scrollY);
	}
}
