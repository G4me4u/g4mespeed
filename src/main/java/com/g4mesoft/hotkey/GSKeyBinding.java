package com.g4mesoft.hotkey;

import com.mojang.blaze3d.platform.InputConstants.Key;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class GSKeyBinding {

	private final GSKeyManager manager;
	private final String name;
	private final String category;
	private final GSMultiKey defaultMultiKey;
	private final boolean allowDisabled;
	
	private int priority;
	
	private GSMultiKey multiKey;
	private boolean[] keyStates;
	private int pressedCount;
	
	private GSIKeyBindingListener listener;
	
	public GSKeyBinding(GSKeyManager manager, String name, String category, GSMultiKey defaultKeyCode, boolean allowDisabled, int priority) {
		this.manager = manager;
		this.name = name;
		this.category = category;
		this.defaultMultiKey = defaultKeyCode;
		this.allowDisabled = allowDisabled;
		
		this.priority = priority;
	
		multiKey = defaultKeyCode;
		keyStates = new boolean[multiKey.getKeyCount()];
		pressedCount = 0;
		
		listener = null;
	}

	public void setKeyListener(GSIKeyBindingListener listener) {
		if (this.listener != null)
			throw new IllegalStateException("The listener for this key binding is already set!");
		this.listener = listener;
	}
	
	void reset() {
		for (int i = 0; i < keyStates.length; i++)
			keyStates[i] = false;
		pressedCount = 0;
	}

	void onKeyPressed(Key key) {
		int count = 0;
		for (int i = 0; i < multiKey.getKeyCount(); i++) {
			if (multiKey.get(i) == key)
				keyStates[i] = true;
			if (keyStates[i])
				count++;
		}
		
		onKeyStateChanged(count);
	}
	
	void onKeyReleased(Key key) {
		int count = 0;
		for (int i = 0; i < multiKey.getKeyCount(); i++) {
			if (multiKey.get(i) == key)
				keyStates[i] = false;
			if (keyStates[i])
				count++;
		}
		
		onKeyStateChanged(count);
	}
	
	private void onKeyStateChanged(int count) {
		boolean wasPressed = isPressed();
		this.pressedCount = count;
		
		if (isPressed() != wasPressed)
			manager.scheduleEvent(this);
	}
	
	void dispatchKeyEvent(GSEKeyEventType eventType) {
		// Use local field to ensure thread safety.
		final GSIKeyBindingListener listener = this.listener;
		if (listener != null) {
			Minecraft client = Minecraft.getInstance();
			if (client.isSameThread()) {
				listener.onKeyStateChanged(this, eventType);
			} else {
				client.execute(() -> listener.onKeyStateChanged(this, eventType));
			}
		}
	}

	public String getName() {
		return name;
	}
	
	public String getCategory() {
		return category;
	}
	
	public GSMultiKey getDefaultKey() {
		return defaultMultiKey;
	}
	
	public GSMultiKey getKey() {
		return multiKey;
	}

	public void setKey(GSMultiKey multiKey) {
		if (!allowDisabled && multiKey == GSMultiKey.UNKNOWN_KEY)
			multiKey = defaultMultiKey;
		
		GSMultiKey oldMultiKey = this.multiKey;
		this.multiKey = multiKey;
		keyStates = new boolean[multiKey.getKeyCount()];
		reset();
		
		manager.onKeyChanged(this, oldMultiKey, multiKey);
	}
	
	public Component getLocalizedName() {
		return multiKey.getLocalizedText();
	}
	
	public boolean isAnyPressed() {
		return (pressedCount != 0);
	}
	
	public boolean isPressed() {
		return (pressedCount == multiKey.getKeyCount());
	}

	/* Higher value of priority means key is dominant */
	public void setPriority(int priority) {
		if (priority < 0)
			throw new IllegalArgumentException("priority must be non-negative");
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}
}
