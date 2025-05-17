package com.g4mesoft.hotkey;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

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
	
	public Text getLocalizedText() {
		if (keys.length > 1) {
			Text text = getLocalizedName(keys[0]);
			for (int i = 1; i < keys.length; i++)
				text.append(" + ").append(getLocalizedName(keys[i]));
			return text;
		}
		return getLocalizedName(keys[0]);
	}
	
	private Text getLocalizedName(InputConstants.Key keyCode) {
		String result = null;
		
		switch (keyCode.getType()) {
		case KEYSYM:
			result = GLFW.glfwGetKeyName(keyCode.getValue(), GLFW.GLFW_KEY_UNKNOWN);
			break;
		case SCANCODE:
			result = GLFW.glfwGetKeyName(GLFW.GLFW_KEY_UNKNOWN, keyCode.getValue());
			break;
		case MOUSE:
			result = I18n.hasTranslation(keyCode.getName()) ? I18n.translate(keyCode.getName()) :
					I18n.translate("key.mouse", keyCode.getValue() + 1);
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
	
	public static GSKeyCode fromType(InputConstants.Type type, int code) {
		return fromKey(type.getOrCreate(code));
	}
	
	public static GSKeyCode fromKeyCode(int keyCode, int scanCode) {
		return fromKey(InputConstants.getKey(keyCode, scanCode));
	}
	
	public static GSKeyCode fromKey(InputConstants.Key key) {
		return fromKeys(key);
	}
	
	public static GSKeyCode fromKeys(InputConstants.Key... keys) {
		if (keys.length == 0)
			throw new IllegalArgumentException("Must contain at least one key!");
		if (keys.length == 1 && keys[0] == InputConstants.UNKNOWN)
			return UNKNOWN_KEY;
		return new GSKeyCode(keys);
	}
}
