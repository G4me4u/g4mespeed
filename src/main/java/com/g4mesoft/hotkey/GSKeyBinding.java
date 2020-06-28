package com.g4mesoft.hotkey;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.text.Text;

public class GSKeyBinding {

	private final GSKeyManager manager;
	private final String name;
	private final String category;
	private final Key defaultKeyCode;
	private final boolean allowDisabled;
	
	private Key keyCode;

	private boolean keyState;
	
	private boolean wasPressed;
	private boolean pressed;
	private int repeatCount;

	private GSIKeyListener listener;
	
	public GSKeyBinding(GSKeyManager manager, String name, String category, InputUtil.Type keyType, int keyCode, boolean allowDisabled) {
		this.manager = manager;
		this.name = name;
		this.category = category;
		this.defaultKeyCode = keyType.createFromCode(keyCode);
		this.allowDisabled = allowDisabled;
	
		this.keyCode = defaultKeyCode;
	
		listener = null;
	}

	public void setKeyListener(GSIKeyListener listener) {
		if (this.listener != null)
			throw new IllegalStateException("The listener for this key binding is already set!");
		this.listener = listener;
	}
	
	public void reset() {
		pressed = wasPressed = false;
		repeatCount = 0;
	}

	public void onKeyPressed() {
		repeatCount++;

		if (!pressed) {
			wasPressed = false;
			pressed = true;
		}

		onStateChanged(true, GSEKeyEventType.PRESS);
	}
	
	public void onKeyReleased() {
		onStateChanged(false, GSEKeyEventType.RELEASE);
	}
	
	private void onStateChanged(boolean newKeyState, GSEKeyEventType eventType) {
		if (newKeyState != this.keyState) {
			this.keyState = newKeyState;
			
			final GSIKeyListener listener = this.listener;
			if (listener != null) {
				MinecraftClient client = MinecraftClient.getInstance();
				if (client.isOnThread()) {
					listener.onKeyStateChanged(this, eventType);
				} else {
					client.execute(() -> listener.onKeyStateChanged(this, eventType));
				}
			}
		}
	}

	public void onKeyRepeated() {
		repeatCount++;
	}
	
	public void update() {
		wasPressed = pressed;
		pressed = keyState;
		
		repeatCount = 0;
	}

	public Text getLocalizedName() {
		return keyCode.getLocalizedText();
	}

	public boolean isPressed() {
		return pressed;
	}

	public boolean wasPressed() {
		return wasPressed;
	}
	
	public boolean isClicking() {
		return pressed && !wasPressed;
	}
	
	public boolean isReleaing() {
		return !pressed && wasPressed;
	}
	
	public int getRepeatCount() {
		return repeatCount;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCategory() {
		return category;
	}
	
	public Key getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(Key keyCode) {
		if (!allowDisabled && keyCode == InputUtil.UNKNOWN_KEY)
			keyCode = defaultKeyCode;
		
		Key oldKeyCode = this.keyCode;
		this.keyCode = keyCode;
		reset();
		
		manager.onKeyCodeChanged(this, oldKeyCode, keyCode);
	}

	public int getGLFWKeyCode() {
		return keyCode.getCode();
	}
	
	public Key getDefaultKeyCode() {
		return defaultKeyCode;
	}
}
