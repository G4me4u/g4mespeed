package com.g4mesoft.gui.event;

import org.lwjgl.glfw.GLFW;

public abstract class GSEvent {
	
	public static final int UNKNOWN_TYPE = 0;
	
	public static final int NO_MODIFIERS = 0;
	
	public static final int MODIFIER_SHIFT     = GLFW.GLFW_MOD_SHIFT;
	public static final int MODIFIER_CONTROL   = GLFW.GLFW_MOD_CONTROL;
	public static final int MODIFIER_ALT       = GLFW.GLFW_MOD_ALT;
	public static final int MODIFIER_SUPER     = GLFW.GLFW_MOD_SUPER;
	public static final int MODIFIER_CAPS_LOCK = GLFW.GLFW_MOD_CAPS_LOCK;
	public static final int MODIFIER_NUM_LOCK  = GLFW.GLFW_MOD_NUM_LOCK;
	
	public static final int ALL_MODIFIERS = MODIFIER_SHIFT     | MODIFIER_CONTROL | 
	                                        MODIFIER_ALT       | MODIFIER_SUPER |
	                                        MODIFIER_CAPS_LOCK | MODIFIER_NUM_LOCK;
	
	private boolean consumed;
	
	public GSEvent() {
		consumed = false;
	}
	
	public abstract int getType();

	public void consume() {
		consumed = true;
	}
	
	public boolean isConsumed() {
		return consumed;
	}
}
