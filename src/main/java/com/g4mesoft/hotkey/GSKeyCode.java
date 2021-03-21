package com.g4mesoft.hotkey;

import java.util.Arrays;

import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class GSKeyCode {

	public static final GSKeyCode UNKNOWN_KEY = new GSKeyCode(new InputUtil.Key[] { InputUtil.UNKNOWN_KEY });
	
	private InputUtil.Key[] keys;
	
	private GSKeyCode(InputUtil.Key[] keys) {
		this.keys = keys;
	}

	public InputUtil.Key get(int index) {
		return keys[index];
	}
	
	public int indexOf(InputUtil.Key key) {
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
			MutableText text = keys[0].getLocalizedText().copy();
			for (int i = 1; i < keys.length; i++)
				text.append(" + ").append(keys[i].getLocalizedText());
			return text;
		}
		return keys[0].getLocalizedText();
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
		return fromKey(InputUtil.fromKeyCode(keyCode, scanCode));
	}
	
	public static GSKeyCode fromKey(Key key) {
		return fromKeys(key);
	}
	
	public static GSKeyCode fromKeys(Key... keys) {
		if (keys.length == 0)
			throw new IllegalArgumentException("Must contain at least one key!");
		if (keys.length == 1 && keys[0] == InputUtil.UNKNOWN_KEY)
			return UNKNOWN_KEY;
		return new GSKeyCode(keys);
	}
}
