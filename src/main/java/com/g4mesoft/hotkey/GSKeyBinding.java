package com.g4mesoft.hotkey;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.text.Text;

public class GSKeyBinding {

	private final GSKeyManager manager;
	private final String name;
	private final String category;
	private final GSKeyCode defaultKeyCode;
	private final boolean allowDisabled;
	
	private int priority;
	
	private GSKeyCode keyCode;
	private boolean[] keyStates;
	private int pressedCount;
	
	private GSIKeyBindingListener listener;
	
	public GSKeyBinding(GSKeyManager manager, String name, String category, GSKeyCode defaultKeyCode, boolean allowDisabled, int priority) {
		this.manager = manager;
		this.name = name;
		this.category = category;
		this.defaultKeyCode = defaultKeyCode;
		this.allowDisabled = allowDisabled;
		
		this.priority = priority;
	
		keyCode = defaultKeyCode;
		keyStates = new boolean[keyCode.getKeyCount()];
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
		for (int i = 0; i < keyCode.getKeyCount(); i++) {
			if (keyCode.get(i) == key)
				keyStates[i] = true;
			if (keyStates[i])
				count++;
		}
		
		onKeyStateChanged(count);
	}
	
	void onKeyReleased(Key key) {
		int count = 0;
		for (int i = 0; i < keyCode.getKeyCount(); i++) {
			if (keyCode.get(i) == key)
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
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.isOnThread()) {
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
	
	public GSKeyCode getDefaultKeyCode() {
		return defaultKeyCode;
	}
	
	public GSKeyCode getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(GSKeyCode keyCode) {
		if (!allowDisabled && keyCode == GSKeyCode.UNKNOWN_KEY)
			keyCode = defaultKeyCode;
		
		GSKeyCode oldKeyCode = this.keyCode;
		this.keyCode = keyCode;
		keyStates = new boolean[keyCode.getKeyCount()];
		reset();
		
		manager.onKeyCodeChanged(this, oldKeyCode, keyCode);
	}
	
	public Text getLocalizedName() {
		return keyCode.getLocalizedText();
	}
	
	public boolean isAnyPressed() {
		return (pressedCount != 0);
	}
	
	public boolean isPressed() {
		return (pressedCount == keyCode.getKeyCount());
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
