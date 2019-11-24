package com.g4mesoft.hotkey;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.KeyCode;

public class GSKeyBinding {

	private final String name;
	private final String category;
	private final KeyCode defaultKeyCode;
	
	private KeyCode keyCode;

	private boolean keyState;
	
	private boolean wasPressed;
	private boolean pressed;
	private int repeatCount;

	private GSIKeyListener listener;
	
	public GSKeyBinding(String name, String category, InputUtil.Type keyType, int keyCode) {
		this.name = name;
		this.category = category;
		this.defaultKeyCode = keyType.createFromCode(keyCode);
	
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
		pressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().window.getHandle(), keyCode.getKeyCode());
	
		repeatCount = 0;
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
	
	public KeyCode getKeyCode() {
		return keyCode;
	}

	public int getGLFWKeyCode() {
		return keyCode.getKeyCode();
	}
	
	public KeyCode getDefaultKeyCode() {
		return defaultKeyCode;
	}
}