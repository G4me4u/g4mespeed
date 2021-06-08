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
	
	private GSKeyCode keyCode;
	private boolean[] keyStates;
	private boolean pressed;

	private GSIKeyBindingListener listener;
	
	public GSKeyBinding(GSKeyManager manager, String name, String category, GSKeyCode defaultKeyCode, boolean allowDisabled) {
		this.manager = manager;
		this.name = name;
		this.category = category;
		this.defaultKeyCode = defaultKeyCode;
		this.allowDisabled = allowDisabled;
	
		keyCode = defaultKeyCode;
		keyStates = new boolean[keyCode.getKeyCount()];
		pressed = false;
		
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
		pressed = false;
	}

	void onKeyPressed(Key key) {
		boolean keyState = true;
		for (int i = 0; i < keyCode.getKeyCount(); i++) {
			if (keyCode.get(i) == key)
				keyStates[i] = true;
			keyState &= keyStates[i];
		}
		
		onKeyStateChanged(keyState);
	}
	
	void onKeyReleased(Key key) {
		for (int i = 0; i < keyCode.getKeyCount(); i++) {
			if (keyCode.get(i) == key)
				keyStates[i] = false;
		}
		
		onKeyStateChanged(false);
	}
	
	private void onKeyStateChanged(boolean keyState) {
		if (keyState != pressed) {
			pressed = keyState;
			
			manager.scheduleEvent(this);
		}
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
	
	public boolean isPressed() {
		return pressed;
	}
}
