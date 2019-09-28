package com.g4mesoft.setting;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.options.KeyBinding;

public class GSClientSettings {

	public final KeyBinding gsIncreaseTpsKey;
	public final KeyBinding gsDecreaseTpsKey;
	public final KeyBinding gsHalfTpsKey;
	public final KeyBinding gsDoubleTpsKey;

	public final KeyBinding gsResetTpsKey;
	
	public GSClientSettings() {
		gsIncreaseTpsKey = new KeyBinding("key.increaseTps", GLFW.GLFW_KEY_PERIOD, "key.categories.g4mespeed");
		gsDecreaseTpsKey = new KeyBinding("key.decreaseTps", GLFW.GLFW_KEY_COMMA, "key.categories.g4mespeed");
		gsHalfTpsKey = new KeyBinding("key.halfTps", GLFW.GLFW_KEY_J, "key.categories.g4mespeed");
		gsDoubleTpsKey = new KeyBinding("key.doubleTps", GLFW.GLFW_KEY_K, "key.categories.g4mespeed");

		gsResetTpsKey = new KeyBinding("key.resetTps", GLFW.GLFW_KEY_M, "key.categories.g4mespeed");
	}
}
