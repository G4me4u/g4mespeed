package com.g4mesoft.hotkey;

import java.util.Arrays;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class GSKeyCode {

	public static final GSKeyCode UNKNOWN_KEY = new GSKeyCode(new InputUtil.KeyCode[] { InputUtil.UNKNOWN_KEYCODE });
	
	private InputUtil.KeyCode[] keys;
	
	private GSKeyCode(InputUtil.KeyCode[] keys) {
		this.keys = keys;
	}

	public InputUtil.KeyCode get(int index) {
		return keys[index];
	}
	
	public int indexOf(InputUtil.KeyCode key) {
		for (int i = 0; i < keys.length; i++) {
			if (key == keys[i])
				return i;
		}
		
		return -1;
	}
	
	public int getKeyCount() {
		return keys.length;
	}
	
	public Text getLocalizedText() {
		if (keys.length > 1) {
			Text text = getLocalizedName(keys[0]);
			for (int i = 1; i < keys.length; i++)
				text.append(" + ").append(getLocalizedName(keys[i]));
			return text;
		}
		return getLocalizedName(keys[0]);
	}
	
	private Text getLocalizedName(InputUtil.KeyCode keyCode) {
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
		
		if (result == null)
			result = I18n.translate(keyCode.getName());
	
		return new LiteralText(result);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(keys);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof GSKeyCode))
			return false;
		return Arrays.equals(keys, ((GSKeyCode)other).keys);
	}
	
	public static GSKeyCode fromType(InputUtil.Type type, int code) {
		return fromKey(type.createFromCode(code));
	}
	
	public static GSKeyCode fromKeyCode(int keyCode, int scanCode) {
		return fromKey(InputUtil.getKeyCode(keyCode, scanCode));
	}
	
	public static GSKeyCode fromKey(InputUtil.KeyCode key) {
		return fromKeys(key);
	}
	
	public static GSKeyCode fromKeys(InputUtil.KeyCode... keys) {
		if (keys.length == 0)
			throw new IllegalArgumentException("Must contain at least one key!");
		if (keys.length == 1 && keys[0] == InputUtil.UNKNOWN_KEYCODE)
			return UNKNOWN_KEY;
		return new GSKeyCode(keys);
	}
}
