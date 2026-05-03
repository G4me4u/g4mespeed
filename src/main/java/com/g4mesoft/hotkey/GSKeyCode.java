package com.g4mesoft.hotkey;

import java.util.Arrays;

import com.g4mesoft.ui.panel.event.GSEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GSKeyCode {

	public static final GSKeyCode UNKNOWN_KEY = new GSKeyCode(new InputConstants.Key[] { InputConstants.UNKNOWN });
	
	private InputConstants.Key[] keys;
	
	private GSKeyCode(InputConstants.Key[] keys) {
		this.keys = keys;
	}

	public InputConstants.Key get(int index) {
		return keys[index];
	}
	
	public int indexOf(InputConstants.Key key) {
		for (int i = 0; i < keys.length; i++) {
			if (key == keys[i])
				return i;
		}
		
		return -1;
	}
	
	public int getKeyCount() {
		return keys.length;
	}
	
	public Component getLocalizedText() {
		if (keys.length > 1) {
			MutableComponent text = keys[0].getDisplayName().copy();
			for (int i = 1; i < keys.length; i++)
				text.append(" + ").append(keys[i].getDisplayName());
			return text;
		}
		return keys[0].getDisplayName();
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
	
	public static GSKeyCode fromType(InputConstants.Type type, int code) {
		return fromKey(type.getOrCreate(code));
	}
	
	public static GSKeyCode fromKeyCode(int keyCode, int scanCode) {
		KeyEvent input = new KeyEvent(keyCode, scanCode, GSEvent.NO_MODIFIERS);
		return fromKey(InputConstants.getKey(input));
	}
	
	public static GSKeyCode fromKey(Key key) {
		return fromKeys(key);
	}
	
	public static GSKeyCode fromKeys(Key... keys) {
		if (keys.length == 0)
			throw new IllegalArgumentException("Must contain at least one key!");
		if (keys.length == 1 && keys[0] == InputConstants.UNKNOWN)
			return UNKNOWN_KEY;
		return new GSKeyCode(keys);
	}
}
