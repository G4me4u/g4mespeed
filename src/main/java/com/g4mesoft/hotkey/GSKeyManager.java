package com.g4mesoft.hotkey;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.g4mesoft.util.GSFileUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;

@Environment(EnvType.CLIENT)
public class GSKeyManager {
	
	private final Map<String, Map<String, Key>> keySettings;

	private final List<GSKeyBinding> keyBindings;
	private final Map<Key, LinkedList<GSKeyBinding>> codeToKeys;

	private GSIKeyRegisterListener registerListener;
	
	public GSKeyManager() {
		keySettings = new HashMap<String, Map<String, Key>>();

		keyBindings = new ArrayList<GSKeyBinding>();
		codeToKeys = new HashMap<Key, LinkedList<GSKeyBinding>>();
	}

	public void loadKeys(File keySettingsFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(keySettingsFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] args = line.split(":");
				if (args.length != 3)
					continue;
				
				Key keyCode;
				try {
					keyCode = InputUtil.fromTranslationKey(args[2]);
				} catch (IllegalArgumentException e) {
					continue;
				}
				
				setKeySetting(args[0], args[1], keyCode);
			}
		} catch (IOException e) {
		}
	}

	public void saveKeys(File keySettingsFile) {
		try {
			GSFileUtils.ensureFileExists(keySettingsFile);
			
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(keySettingsFile))) {
				for (Map.Entry<String, Map<String, Key>> categorySettings : keySettings.entrySet()) {
					String category = categorySettings.getKey();
					for (Map.Entry<String, Key> setting : categorySettings.getValue().entrySet()) {
						bw.write(category);
						bw.write(':');
						bw.write(setting.getKey());
						bw.write(':');
						bw.write(setting.getValue().getTranslationKey());
						bw.newLine();
					}
				}
			}
		} catch (IOException e) {
		}
	}
	
	private Key getKeySetting(String category, String keyName) {
		synchronized (keySettings) {
			Map<String, Key> categorySettings = keySettings.get(category);
			return (categorySettings == null) ? null : categorySettings.get(keyName);
		}
	}

	private void setKeySetting(String category, String keyName, Key keyCode) {
		synchronized (keySettings) {
			Map<String, Key> categorySettings = keySettings.get(category);
			if (categorySettings == null) {
				categorySettings = new HashMap<String, Key>();
				keySettings.put(category, categorySettings);
			}
			
			categorySettings.put(keyName, keyCode);
		}
	}
	
	public void update() {
		for (GSKeyBinding keyBinding : keyBindings)
			keyBinding.update();
	}
	
	public <T> GSKeyBinding registerKey(String name, String category, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		return registerKey(name, category, keyCode, listenerData, listener, eventType, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType, boolean allowDisabled) {
		return registerKey(name, category, InputUtil.Type.KEYSYM, keyCode, listenerData, listener, eventType, allowDisabled);
	}

	public <T> GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		return registerKey(name, category, keyType, keyCode, listenerData, listener, eventType, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType, boolean allowDisabled) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");
		
		return registerKey(name, category, keyType, keyCode, (key, type) -> {
			if (type == eventType)
				listener.accept(listenerData);
		}, allowDisabled);
	}

	public <T> GSKeyBinding registerKey(String name, String category, int keyCode, T listenerData, BiConsumer<T, GSEKeyEventType> listener) {
		return registerKey(name, category, keyCode, listenerData, listener, true);
	}
	
	public <T> GSKeyBinding registerKey(String name, String category, int keyCode, T listenerData, BiConsumer<T, GSEKeyEventType> listener, boolean allowDisabled) {
		return registerKey(name, category, InputUtil.Type.KEYSYM, keyCode, listenerData, listener, allowDisabled);
	}

	public <T> GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode, T listenerData, BiConsumer<T, GSEKeyEventType> listener) {
		return registerKey(name, category, keyType, keyCode, listenerData, listener, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode, T listenerData, BiConsumer<T, GSEKeyEventType> listener, boolean allowDisabled) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");

		return registerKey(name, category, keyType, keyCode, (key, type) -> listener.accept(listenerData, type), allowDisabled);
	}

	public GSKeyBinding registerKey(String name, String category, int keyCode) {
		return registerKey(name, category, keyCode, true);
	}

	public GSKeyBinding registerKey(String name, String category, int keyCode, boolean allowDisabled) {
		return registerKey(name, category, InputUtil.Type.KEYSYM, keyCode, allowDisabled);
	}

	public GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode) {
		return registerKey(name, category, keyType, keyCode, true);
	}

	public GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode, boolean allowDisabled) {
		return registerKey(name, category, keyType, keyCode, null, allowDisabled);
	}

	public GSKeyBinding registerKey(String name, String category, int keyCode, GSIKeyListener listener) {
		return registerKey(name, category, keyCode, listener, true);
	}

	public GSKeyBinding registerKey(String name, String category, int keyCode, GSIKeyListener listener, boolean allowDisabled) {
		return registerKey(name, category, InputUtil.Type.KEYSYM, keyCode, listener, allowDisabled);
	}

	public GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode, GSIKeyListener listener) {
		return registerKey(name, category, keyType, keyCode, listener, true);
	}

	public GSKeyBinding registerKey(String name, String category, InputUtil.Type keyType, int keyCode, GSIKeyListener listener, boolean allowDisabled) {
		if (name.contains(":") || category.contains(":"))
			throw new IllegalArgumentException("Invalid name or category! It must not contains ':'!");
		
		GSKeyBinding keyBinding = new GSKeyBinding(this, name, category, keyType, keyCode, allowDisabled);
		keyBinding.setKeyListener(listener);
		addKeyBinding(keyBinding);
		
		if (registerListener != null)
			registerListener.onKeyRegistered(keyBinding);
	
		return keyBinding;
	}
	
	private void addKeyBinding(GSKeyBinding keyBinding) {
		keyBindings.add(keyBinding);
		addKeyCodeMapping(keyBinding);
		
		Key keyCodeSetting = getKeySetting(keyBinding.getCategory(), keyBinding.getName());
		if (keyCodeSetting != null) {
			keyBinding.setKeyCode(keyCodeSetting);
		} else {
			setKeySetting(keyBinding.getCategory(), keyBinding.getName(), keyBinding.getKeyCode());
		}
	}
	
	public void setKeyRegisterListener(GSIKeyRegisterListener registerListener) {
		this.registerListener = registerListener;
	}

	private void handleKeyEvent(Key keyCode, Consumer<GSKeyBinding> eventMethod) {
		synchronized(codeToKeys) {
			List<GSKeyBinding> keys = codeToKeys.get(keyCode);
			if (keys != null) {
				for (GSKeyBinding key : keys)
					eventMethod.accept(key);
			}
		}
	}
	
	protected void onKeyCodeChanged(GSKeyBinding keyBinding, Key oldKeyCode, Key keyCode) {
		synchronized(codeToKeys) {
			List<GSKeyBinding> keysWithOldCode = codeToKeys.get(oldKeyCode);
			if (keysWithOldCode != null) {
				keysWithOldCode.remove(keyBinding);
				if (keysWithOldCode.isEmpty())
					codeToKeys.remove(oldKeyCode);
			}
		}
		
		addKeyCodeMapping(keyBinding);
		
		setKeySetting(keyBinding.getCategory(), keyBinding.getName(), keyCode);
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
		handleKeyEvent(InputUtil.fromKeyCode(key, scancode), GSKeyBinding::onKeyPressed);
	}

	public void onKeyReleased(int key, int scancode, int mods) {
		handleKeyEvent(InputUtil.fromKeyCode(key, scancode), GSKeyBinding::onKeyReleased);
	}

	public void onKeyRepeat(int key, int scancode, int mods) {
		handleKeyEvent(InputUtil.fromKeyCode(key, scancode), GSKeyBinding::onKeyRepeated);
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
