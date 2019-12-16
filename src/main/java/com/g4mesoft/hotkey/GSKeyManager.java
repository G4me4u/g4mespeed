package com.g4mesoft.hotkey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.KeyCode;

@Environment(EnvType.CLIENT)
public class GSKeyManager {

	private final List<GSKeyBinding> keyBindings;
	private final Map<KeyCode, LinkedList<GSKeyBinding>> codeToKeys;

	private GSIKeyRegisterListener registerListener;
	
	public GSKeyManager() {
		keyBindings = new ArrayList<GSKeyBinding>();
		codeToKeys = new HashMap<KeyCode, LinkedList<GSKeyBinding>>();
	}

	public void update() {
		for (GSKeyBinding keyBinding : keyBindings)
			keyBinding.update();
	}
	
	public <T> void registerKey(String name, String category, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		registerKey(name, category, InputUtil.Type.KEYSYM, keyCode, listenerData, listener, eventType);
	}

	public <T> void registerKey(String name, String category, InputUtil.Type keyType, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");
		
		registerKey(name, category, keyType, keyCode, (key, type) -> {
			if (type == eventType)
				listener.accept(listenerData);
		});
	}
	
	public <T> void registerKey(String name, String category, int keyCode, T listenerData, BiConsumer<T, GSEKeyEventType> listener) {
		registerKey(name, category, InputUtil.Type.KEYSYM, keyCode, listenerData, listener);
	}

	public <T> void registerKey(String name, String category, InputUtil.Type keyType, int keyCode, T listenerData, BiConsumer<T, GSEKeyEventType> listener) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");

		registerKey(name, category, keyType, keyCode, (key, type) -> listener.accept(listenerData, type));
	}

	public void registerKey(String name, String category, int keyCode) {
		registerKey(name, category, InputUtil.Type.KEYSYM, keyCode);
	}

	public void registerKey(String name, String category, InputUtil.Type keyType, int keyCode) {
		registerKey(name, category, keyType, keyCode, null);
	}

	public void registerKey(String name, String category, int keyCode, GSIKeyListener listener) {
		registerKey(name, category, InputUtil.Type.KEYSYM, keyCode, listener);
	}

	public void registerKey(String name, String category, InputUtil.Type keyType, int keyCode, GSIKeyListener listener) {
		GSKeyBinding keyBinding = new GSKeyBinding(this, name, category, keyType, keyCode);
		keyBinding.setKeyListener(listener);
		addKeyBinding(keyBinding);
		
		if (registerListener != null)
			registerListener.onKeyRegistered(keyBinding);
	}
	
	private void addKeyBinding(GSKeyBinding keyBinding) {
		keyBindings.add(keyBinding);
		addKeyCodeMapping(keyBinding);
	}
	
	public void setKeyRegisterListener(GSIKeyRegisterListener registerListener) {
		this.registerListener = registerListener;
	}

	private void handleKeyEvent(KeyCode keyCode, Consumer<GSKeyBinding> eventMethod) {
		synchronized(codeToKeys) {
			List<GSKeyBinding> keys = codeToKeys.get(keyCode);
			if (keys != null) {
				for (GSKeyBinding key : keys)
					eventMethod.accept(key);
			}
		}
	}
	
	protected void onKeyCodeChanged(GSKeyBinding keyBinding, KeyCode oldKeyCode, KeyCode keyCode) {
		synchronized(codeToKeys) {
			List<GSKeyBinding> keysWithOldCode = codeToKeys.get(oldKeyCode);
			if (keysWithOldCode != null) {
				keysWithOldCode.remove(keyBinding);
				if (keysWithOldCode.isEmpty())
					codeToKeys.remove(oldKeyCode);
			}
		}
		
		addKeyCodeMapping(keyBinding);
	}
	
	private void addKeyCodeMapping(GSKeyBinding keyBinding) {
		synchronized(codeToKeys) {
			LinkedList<GSKeyBinding> keysWithCode = codeToKeys.get(keyBinding.getKeyCode());
			if (keysWithCode == null) {
				keysWithCode = new LinkedList<GSKeyBinding>();
				codeToKeys.put(keyBinding.getKeyCode(), keysWithCode);
			}
			keysWithCode.add(keyBinding);
		}
	}
	
	public void onKeyPressed(int key, int scancode, int mods) {
		handleKeyEvent(InputUtil.getKeyCode(key, scancode), GSKeyBinding::onKeyPressed);
	}

	public void onKeyReleased(int key, int scancode, int mods) {
		handleKeyEvent(InputUtil.getKeyCode(key, scancode), GSKeyBinding::onKeyReleased);
	}

	public void onKeyRepeat(int key, int scancode, int mods) {
		handleKeyEvent(InputUtil.getKeyCode(key, scancode), GSKeyBinding::onKeyRepeated);
	}

	public void onMousePressed(int button, int mods) {
		handleKeyEvent(InputUtil.Type.MOUSE.createFromCode(button), GSKeyBinding::onKeyPressed);
	}

	public void onMouseReleased(int button, int mods) {
		handleKeyEvent(InputUtil.Type.MOUSE.createFromCode(button), GSKeyBinding::onKeyReleased);
	}
	
	public List<GSKeyBinding> getKeyBindings() {
		return keyBindings;
	}
}
