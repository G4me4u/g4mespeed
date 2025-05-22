package com.g4mesoft.hotkey;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.g4mesoft.ui.util.GSTextUtil;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

public class GSKey {

	private static final Map<GSEKeyType, Map<Integer, GSKey>> typeToKeys = new EnumMap<>(GSEKeyType.class);
	private static final Map<String, GSKey> nameToKey = new HashMap<>();

	/** Unknown key/mouse button */
	public static final GSKey UNKNOWN           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NONE, "key.keyboard.unknown");

	/** Printable keys. */
	public static final GSKey KEY_SPACE         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_SPACE, "key.keyboard.space");
	public static final GSKey KEY_APOSTROPHE    = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_APOSTROPHE, "key.keyboard.apostrophe");
	public static final GSKey KEY_COMMA         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_COMMA, "key.keyboard.comma");
	public static final GSKey KEY_MINUS         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_MINUS, "key.keyboard.minus");
	public static final GSKey KEY_PERIOD        = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_PERIOD, "key.keyboard.period");
	public static final GSKey KEY_SLASH         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_SLASH, "key.keyboard.slash");
	public static final GSKey KEY_0             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_0, "key.keyboard.0");
	public static final GSKey KEY_1             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_1, "key.keyboard.1");
	public static final GSKey KEY_2             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_2, "key.keyboard.2");
	public static final GSKey KEY_3             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_3, "key.keyboard.3");
	public static final GSKey KEY_4             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_4, "key.keyboard.4");
	public static final GSKey KEY_5             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_5, "key.keyboard.5");
	public static final GSKey KEY_6             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_6, "key.keyboard.6");
	public static final GSKey KEY_7             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_7, "key.keyboard.7");
	public static final GSKey KEY_8             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_8, "key.keyboard.8");
	public static final GSKey KEY_9             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_9, "key.keyboard.9");
	public static final GSKey KEY_SEMICOLON     = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_SEMICOLON, "key.keyboard.semicolon");
	public static final GSKey KEY_EQUAL         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_EQUALS, "key.keyboard.equal");
	public static final GSKey KEY_A             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_A, "key.keyboard.a");
	public static final GSKey KEY_B             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_B, "key.keyboard.b");
	public static final GSKey KEY_C             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_C, "key.keyboard.c");
	public static final GSKey KEY_D             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_D, "key.keyboard.d");
	public static final GSKey KEY_E             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_E, "key.keyboard.e");
	public static final GSKey KEY_F             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F, "key.keyboard.f");
	public static final GSKey KEY_G             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_G, "key.keyboard.g");
	public static final GSKey KEY_H             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_H, "key.keyboard.h");
	public static final GSKey KEY_I             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_I, "key.keyboard.i");
	public static final GSKey KEY_J             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_J, "key.keyboard.j");
	public static final GSKey KEY_K             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_K, "key.keyboard.k");
	public static final GSKey KEY_L             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_L, "key.keyboard.l");
	public static final GSKey KEY_M             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_M, "key.keyboard.m");
	public static final GSKey KEY_N             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_N, "key.keyboard.n");
	public static final GSKey KEY_O             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_O, "key.keyboard.o");
	public static final GSKey KEY_P             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_P, "key.keyboard.p");
	public static final GSKey KEY_Q             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_Q, "key.keyboard.q");
	public static final GSKey KEY_R             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_R, "key.keyboard.r");
	public static final GSKey KEY_S             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_S, "key.keyboard.s");
	public static final GSKey KEY_T             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_T, "key.keyboard.t");
	public static final GSKey KEY_U             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_U, "key.keyboard.u");
	public static final GSKey KEY_V             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_V, "key.keyboard.v");
	public static final GSKey KEY_W             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_W, "key.keyboard.w");
	public static final GSKey KEY_X             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_X, "key.keyboard.x");
	public static final GSKey KEY_Y             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_Y, "key.keyboard.y");
	public static final GSKey KEY_Z             = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_Z, "key.keyboard.z");
	public static final GSKey KEY_LEFT_BRACKET  = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_LBRACKET, "key.keyboard.left.bracket");
	public static final GSKey KEY_BACKSLASH     = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_BACKSLASH, "key.keyboard.backslash");
	public static final GSKey KEY_RIGHT_BRACKET = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_RBRACKET, "key.keyboard.right.bracket");
	public static final GSKey KEY_GRAVE_ACCENT  = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_GRAVE, "key.keyboard.grave.accent");
	
	/** Function keys. */
	public static final GSKey KEY_ESCAPE        = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_ESCAPE, "key.keyboard.escape");
	public static final GSKey KEY_ENTER         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_RETURN, "key.keyboard.enter");
	public static final GSKey KEY_TAB           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_TAB, "key.keyboard.tab");
	public static final GSKey KEY_BACKSPACE     = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_BACK, "key.keyboard.backspace");
	public static final GSKey KEY_INSERT        = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_INSERT, "key.keyboard.insert");
	public static final GSKey KEY_DELETE        = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_DELETE, "key.keyboard.delete");
	public static final GSKey KEY_RIGHT         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_RIGHT, "key.keyboard.right");
	public static final GSKey KEY_LEFT          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_LEFT, "key.keyboard.left");
	public static final GSKey KEY_DOWN          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_DOWN, "key.keyboard.down");
	public static final GSKey KEY_UP            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_UP, "key.keyboard.up");
	public static final GSKey KEY_PAGE_UP       = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_PRIOR, "key.keyboard.page.up");
	public static final GSKey KEY_PAGE_DOWN     = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NEXT, "key.keyboard.page.down");
	public static final GSKey KEY_HOME          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_HOME, "key.keyboard.home");
	public static final GSKey KEY_END           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_END, "key.keyboard.end");
	public static final GSKey KEY_CAPS_LOCK     = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_CAPITAL, "key.keyboard.caps.lock");
	public static final GSKey KEY_SCROLL_LOCK   = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_SCROLL, "key.keyboard.scroll.lock");
	public static final GSKey KEY_NUM_LOCK      = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMLOCK, "key.keyboard.num.lock");
	public static final GSKey KEY_PRINT_SCREEN  = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_SYSRQ, "key.keyboard.print.screen");
	public static final GSKey KEY_PAUSE         = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_PAUSE, "key.keyboard.pause");
	public static final GSKey KEY_F1            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F1, "key.keyboard.f1");
	public static final GSKey KEY_F2            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F2, "key.keyboard.f2");
	public static final GSKey KEY_F3            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F3, "key.keyboard.f3");
	public static final GSKey KEY_F4            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F4, "key.keyboard.f4");
	public static final GSKey KEY_F5            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F5, "key.keyboard.f5");
	public static final GSKey KEY_F6            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F6, "key.keyboard.f6");
	public static final GSKey KEY_F7            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F7, "key.keyboard.f7");
	public static final GSKey KEY_F8            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F8, "key.keyboard.f8");
	public static final GSKey KEY_F9            = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F9, "key.keyboard.f9");
	public static final GSKey KEY_F10           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F10, "key.keyboard.f10");
	public static final GSKey KEY_F11           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F11, "key.keyboard.f11");
	public static final GSKey KEY_F12           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F12, "key.keyboard.f12");
	public static final GSKey KEY_F13           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F13, "key.keyboard.f13");
	public static final GSKey KEY_F14           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F14, "key.keyboard.f14");
	public static final GSKey KEY_F15           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F15, "key.keyboard.f15");
	public static final GSKey KEY_F16           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F16, "key.keyboard.f16");
	public static final GSKey KEY_F17           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F17, "key.keyboard.f17");
	public static final GSKey KEY_F18           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F18, "key.keyboard.f18");
	public static final GSKey KEY_F19           = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_F19, "key.keyboard.f19");
	public static final GSKey KEY_KP_0          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD0, "key.keyboard.keypad.0");
	public static final GSKey KEY_KP_1          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD1, "key.keyboard.keypad.1");
	public static final GSKey KEY_KP_2          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD2, "key.keyboard.keypad.2");
	public static final GSKey KEY_KP_3          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD3, "key.keyboard.keypad.3");
	public static final GSKey KEY_KP_4          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD4, "key.keyboard.keypad.4");
	public static final GSKey KEY_KP_5          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD5, "key.keyboard.keypad.5");
	public static final GSKey KEY_KP_6          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD6, "key.keyboard.keypad.6");
	public static final GSKey KEY_KP_7          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD7, "key.keyboard.keypad.7");
	public static final GSKey KEY_KP_8          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD8, "key.keyboard.keypad.8");
	public static final GSKey KEY_KP_9          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPAD9, "key.keyboard.keypad.9");
	public static final GSKey KEY_KP_DECIMAL    = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_DECIMAL, "key.keyboard.keypad.decimal");
	public static final GSKey KEY_KP_DIVIDE     = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_DIVIDE, "key.keyboard.keypad.divide");
	public static final GSKey KEY_KP_MULTIPLY   = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_MULTIPLY, "key.keyboard.keypad.multiply");
	public static final GSKey KEY_KP_SUBTRACT   = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_SUBTRACT, "key.keyboard.keypad.subtract");
	public static final GSKey KEY_KP_ADD        = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_ADD, "key.keyboard.keypad.add");
	public static final GSKey KEY_KP_ENTER      = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPADENTER, "key.keyboard.keypad.enter");
	public static final GSKey KEY_KP_EQUAL      = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_NUMPADEQUALS, "key.keyboard.keypad.equal");
	public static final GSKey KEY_LEFT_SHIFT    = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_LSHIFT, "key.keyboard.left.shift");
	public static final GSKey KEY_LEFT_CONTROL  = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_LCONTROL, "key.keyboard.left.control");
	public static final GSKey KEY_LEFT_ALT      = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_LMENU, "key.keyboard.left.alt");
	public static final GSKey KEY_LEFT_SUPER    = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_LMETA, "key.keyboard.left.win");
	public static final GSKey KEY_RIGHT_SHIFT   = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_RSHIFT, "key.keyboard.right.shift");
	public static final GSKey KEY_RIGHT_CONTROL = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_RCONTROL, "key.keyboard.right.control");
	public static final GSKey KEY_RIGHT_ALT     = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_RMENU, "key.keyboard.right.alt");
	public static final GSKey KEY_RIGHT_SUPER   = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_RMETA, "key.keyboard.right.win");
	public static final GSKey KEY_MENU          = registerKey(GSEKeyType.KEYBOARD, Keyboard.KEY_APPS, "key.keyboard.menu");
	
	/* Mouse buttons (in LWJGL 2 are 0 to Mouse.getButtonCount() - 1) */
	public static final GSKey MOUSE_BUTTON_1 = registerKey(GSEKeyType.MOUSE, 0, "key.mouse.left");
	public static final GSKey MOUSE_BUTTON_2 = registerKey(GSEKeyType.MOUSE, 1, "key.mouse.right");
	public static final GSKey MOUSE_BUTTON_3 = registerKey(GSEKeyType.MOUSE, 2, "key.mouse.middle");
	public static final GSKey MOUSE_BUTTON_4 = registerKey(GSEKeyType.MOUSE, 3, "key.mouse.4");
	public static final GSKey MOUSE_BUTTON_5 = registerKey(GSEKeyType.MOUSE, 4, "key.mouse.5");
	public static final GSKey MOUSE_BUTTON_6 = registerKey(GSEKeyType.MOUSE, 5, "key.mouse.6");
	public static final GSKey MOUSE_BUTTON_7 = registerKey(GSEKeyType.MOUSE, 6, "key.mouse.7");
	public static final GSKey MOUSE_BUTTON_8 = registerKey(GSEKeyType.MOUSE, 7, "key.mouse.8");

	public static final GSKey MOUSE_BUTTON_LEFT   = MOUSE_BUTTON_1;
	public static final GSKey MOUSE_BUTTON_MIDDLE = MOUSE_BUTTON_3;
	public static final GSKey MOUSE_BUTTON_RIGHT  = MOUSE_BUTTON_2;
	
	private final GSEKeyType type;
	private final int value;
	private final String name;
	
	private GSKey(GSEKeyType type, int value, String name) {
		this.type = type;
		this.value = value;
		this.name = name;
	}
	
	public GSEKeyType getType() {
		return type;
	}
	
	public int getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}
	
	public Text getLocalizedName() {
		String result = null;
		
		switch (type) {
		case KEYBOARD:
			result = Keyboard.getKeyName(value);
			if (result == null)
				result = Character.toString((char)(value - 256)).toUpperCase();
			break;
		case MOUSE:
			switch (value) {
			case 0:
				result = I18n.translate("key.mouse.left");
				break;
			case 1:
				result = I18n.translate("key.mouse.right");
				break;
			case 2:
				result = I18n.translate("key.mouse.middle");
				break;
			default:
				result = I18n.translate("key.mouseButton", value + 1);
				break;
			}
			break;
		}
	
		return GSTextUtil.literal(result);
	}
	
	public static GSKey fromKeyCode(GSEKeyType type, int value) {
		Map<Integer, GSKey> valueToKey = typeToKeys.get(type);
		if (valueToKey == null)
			return UNKNOWN;
		return valueToKey.getOrDefault(Integer.valueOf(value), UNKNOWN);
	}

	public static GSKey fromName(String name) {
		GSKey key = nameToKey.get(name);
		if (key == null)
			throw new IllegalArgumentException("Unknown key name: " + name);
		return key;
	}
	
	private static GSKey registerKey(GSEKeyType type, int value, String name) {
		Map<Integer, GSKey> valueToKey = typeToKeys.get(type);
		if (valueToKey == null) {
			valueToKey = new HashMap<>();
			typeToKeys.put(type, valueToKey);
		}
		GSKey key = new GSKey(type, value, name);
		if (valueToKey.put(Integer.valueOf(value), key) != null)
			throw new IllegalArgumentException("Duplicate key code: " + value);
		if (nameToKey.put(name, key) != null)
			throw new IllegalArgumentException("Duplicate key name: " + name);
		return key;
	}
}
