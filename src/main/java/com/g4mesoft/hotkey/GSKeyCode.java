package com.g4mesoft.hotkey;

import java.util.Arrays;

import net.minecraft.text.Text;

public class GSKeyCode {

	public static final GSKeyCode UNKNOWN_KEY = new GSKeyCode(new GSKey[] { GSKey.UNKNOWN });
	
	private GSKey[] keys;
	
	private GSKeyCode(GSKey[] keys) {
		this.keys = keys;
	}

	public GSKey get(int index) {
		return keys[index];
	}
	
	public int indexOf(GSKey key) {
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
		Text text = keys[0].getLocalizedName();
		for (int i = 1; i < keys.length; i++)
			text.append(" + ").append(keys[i].getLocalizedName());
		return text;
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
	
	public static GSKeyCode fromType(GSEKeyType type, int code) {
		return fromKey(GSKey.fromKeyCode(type, code));
	}
	
	public static GSKeyCode fromKey(GSKey key) {
		return fromKeys(key);
	}
	
	public static GSKeyCode fromKeys(GSKey... keys) {
		if (keys.length == 0)
			throw new IllegalArgumentException("Must contain at least one key!");
		if (keys.length == 1 && keys[0] == GSKey.UNKNOWN)
			return UNKNOWN_KEY;
		return new GSKeyCode(keys);
	}
}
