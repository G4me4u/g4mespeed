package com.g4mesoft.settings;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.options.KeyBinding;

public class GSSettings {

	private boolean gsEnabled;
	private boolean gsShiftPitch;
	
	public final KeyBinding gsIncreaseTpsKey;
	public final KeyBinding gsDecreaseTpsKey;
	public final KeyBinding gsHalfTpsKey;
	public final KeyBinding gsDoubleTpsKey;

	public final KeyBinding gsResetTpsKey;
	
	public GSSettings() {
		gsEnabled = true;
		gsShiftPitch = true;
		
		gsIncreaseTpsKey = new KeyBinding("key.increaseTps", GLFW.GLFW_KEY_PERIOD, "key.categories.g4mespeed");
		gsDecreaseTpsKey = new KeyBinding("key.decreaseTps", GLFW.GLFW_KEY_COMMA, "key.categories.g4mespeed");
		gsHalfTpsKey = new KeyBinding("key.halfTps", GLFW.GLFW_KEY_J, "key.categories.g4mespeed");
		gsDoubleTpsKey = new KeyBinding("key.doubleTps", GLFW.GLFW_KEY_K, "key.categories.g4mespeed");

		gsResetTpsKey = new KeyBinding("key.resetTps", GLFW.GLFW_KEY_M, "key.categories.g4mespeed");
	}
	
	public boolean isEnabled() {
		return gsEnabled;
	}
	
	public void setEnabled(boolean enabled) {
		gsEnabled = enabled;
	}

	public boolean isShiftPitchEnabled() {
		return gsShiftPitch && gsEnabled;
	}
	
	public void setShiftPitch(boolean enabled) {
		gsShiftPitch = enabled;
	}
}
