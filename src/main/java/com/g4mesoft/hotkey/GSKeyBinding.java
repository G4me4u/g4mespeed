package com.g4mesoft.hotkey;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.KeyCode;

public class GSKeyBinding {

	private final GSKeyManager manager;
	private final String name;
	private final String category;
	private final KeyCode defaultKeyCode;
	private final boolean allowDisabled;
	
	private KeyCode keyCode;

	private boolean keyState;
	
	private boolean wasPressed;
	private boolean pressed;
	private int repeatCount;

	private GSIKeyBindingListener listener;
	
	public GSKeyBinding(GSKeyManager manager, String name, String category, InputUtil.Type keyType, int keyCode, boolean allowDisabled) {
		this.manager = manager;
		this.name = name;
		this.category = category;
		this.defaultKeyCode = keyType.createFromCode(keyCode);
		this.allowDisabled = allowDisabled;
	
		this.keyCode = defaultKeyCode;
	
		listener = null;
	}

	public void setKeyListener(GSIKeyBindingListener listener) {
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
	}

	public void onKeyRepeated() {
		repeatCount++;
	}
	
	public void update() {
		wasPressed = pressed;
		pressed = keyState;
		
		repeatCount = 0;
	}

	public String getLocalizedName() {
		String result = null;
		
		switch (keyCode.getCategory()) {
		case KEYSYM:
			result = InputUtil.getKeycodeName(keyCode.getKeyCode());
			break;
		case SCANCODE:
			result = InputUtil.getScancodeName(keyCode.getKeyCode());
			break;
		case MOUSE:
			result = I18n.hasTranslation(keyCode.getName()) ? I18n.translate(keyCode.getName()) :
				I18n.translate(keyCode.getCategory().getName(), keyCode.getKeyCode() + 1);
			break;
		}
		
		return result != null ? result : I18n.translate(keyCode.getName());
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

	public void setKeyCode(KeyCode keyCode) {
		if (!allowDisabled && keyCode == InputUtil.UNKNOWN_KEYCODE)
			keyCode = defaultKeyCode;
		
		KeyCode oldKeyCode = this.keyCode;
		this.keyCode = keyCode;
		reset();
		
		manager.onKeyCodeChanged(this, oldKeyCode, keyCode);
	}

	public KeyCode getDefaultKeyCode() {
		return defaultKeyCode;
	}
}
