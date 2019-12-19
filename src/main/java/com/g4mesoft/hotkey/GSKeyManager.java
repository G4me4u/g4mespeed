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
import net.minecraft.client.util.InputUtil.KeyCode;

@Environment(EnvType.CLIENT)
public class GSKeyManager {
	
	private final Map<String, Map<String, KeyCode>> keySettings;

	private final List<GSKeyBinding> keyBindings;
	private final Map<KeyCode, LinkedList<GSKeyBinding>> codeToKeys;

	private GSIKeyRegisterListener registerListener;
	
	public GSKeyManager() {
		keySettings = new HashMap<String, Map<String,KeyCode>>();

		keyBindings = new ArrayList<GSKeyBinding>();
		codeToKeys = new HashMap<KeyCode, LinkedList<GSKeyBinding>>();
	}

	public void loadKeys(File keySettingsFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(keySettingsFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] args = line.split(":");
				if (args.length != 3)
					continue;
				
				KeyCode keyCode;
				try {
					keyCode = InputUtil.fromName(args[2]);
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
				for (Map.Entry<String, Map<String, KeyCode>> categorySettings : keySettings.entrySet()) {
					String category = categorySettings.getKey();
					for (Map.Entry<String, KeyCode> setting : categorySettings.getValue().entrySet()) {
						bw.write(category);
						bw.write(':');
						bw.write(setting.getKey());
						bw.write(':');
						bw.write(setting.getValue().getName());
						bw.newLine();
					}
				}
			}
		} catch (IOException e) {
		}
	}
	
	private KeyCode getKeySetting(String category, String keyName) {
		synchronized (keySettings) {
			Map<String, KeyCode> categorySettings = keySettings.get(category);
			if (categorySettings == null)
				return null;
			return categorySettings.get(keyName);
		}
	}

	private void setKeySetting(String category, String keyName, KeyCode keyCode) {
		synchronized (keySettings) {
			Map<String, KeyCode> categorySettings = keySettings.get(category);
			if (categorySettings == null) {
				categorySettings = new HashMap<String, KeyCode>();
				keySettings.put(category, categorySettings);
			}
			categorySettings.put(keyName, keyCode);
		}
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
		if (name.contains(":") || category.contains(":"))
			throw new IllegalArgumentException("Invalid name or category! It must not contains ':'!");
		
		GSKeyBinding keyBinding = new GSKeyBinding(this, name, category, keyType, keyCode);
		keyBinding.setKeyListener(listener);
		addKeyBinding(keyBinding);
		
		if (registerListener != null)
			registerListener.onKeyRegistered(keyBinding);
	}
	
	private void addKeyBinding(GSKeyBinding keyBinding) {
		keyBindings.add(keyBinding);
		addKeyCodeMapping(keyBinding);
		
		KeyCode keyCodeSetting = getKeySetting(keyBinding.getCategory(), keyBinding.getName());
		if (keyCodeSetting != null) {
			keyBinding.setKeyCode(keyCodeSetting);
		} else {
			setKeySetting(keyBinding.getCategory(), keyBinding.getName(), keyBinding.getKeyCode());
		}
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
