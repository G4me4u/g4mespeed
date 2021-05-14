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

import com.g4mesoft.util.GSFileUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class GSKeyManager {
	
	private final Map<String, Map<String, GSKeyCode>> keySettings;

	private final List<GSKeyBinding> keyBindings;
	private final Map<InputUtil.Key, LinkedList<GSKeyBinding>> codeToKeys;
	private final LinkedList<GSKeyBinding> eventQueue;

	private GSIKeyBindingRegisterListener registerListener;
	
	public GSKeyManager() {
		keySettings = new HashMap<>();

		keyBindings = new ArrayList<>();
		codeToKeys = new HashMap<>();
		eventQueue = new LinkedList<>();
	}

	public void dispose() {
		keySettings.clear();

		keyBindings.clear();
		codeToKeys.clear();
	}
	
	public void loadKeys(File keySettingsFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(keySettingsFile))) {
			String line;
			outer: while ((line = br.readLine()) != null) {
				String[] args = line.split(":");
				if (args.length != 3)
					continue outer;
				
				String[] keyArgs = args[2].split(",");
				if (keyArgs.length == 0)
					continue;
				
				InputUtil.Key[] keys = new InputUtil.Key[keyArgs.length];
				for (int i = 0; i < keyArgs.length; i++) {
					try {
						keys[i] = InputUtil.fromTranslationKey(keyArgs[i]);
					} catch (IllegalArgumentException e) {
						continue outer;
					}
				}
				
				setKeySetting(args[0], args[1], GSKeyCode.fromKeys(keys));
			}
		} catch (IOException e) {
		}
	}

	public void saveKeys(File keySettingsFile) {
		try {
			GSFileUtil.ensureFileExists(keySettingsFile);
			
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(keySettingsFile))) {
				for (Map.Entry<String, Map<String, GSKeyCode>> categorySettings : keySettings.entrySet()) {
					String category = categorySettings.getKey();
					for (Map.Entry<String, GSKeyCode> setting : categorySettings.getValue().entrySet()) {
						bw.write(category);
						bw.write(':');
						bw.write(setting.getKey());
						bw.write(':');
						GSKeyCode keyCode = setting.getValue();
						for (int i = 0; i < keyCode.getKeyCount(); i++) {
							if (i != 0)
								bw.write(',');
							bw.write(keyCode.get(i).getTranslationKey());
						}
						bw.newLine();
					}
				}
			}
		} catch (IOException e) {
		}
	}
	
	private GSKeyCode getKeySetting(String category, String keyName) {
		synchronized (keySettings) {
			Map<String, GSKeyCode> categorySettings = keySettings.get(category);
			return (categorySettings == null) ? null : categorySettings.get(keyName);
		}
	}

	private void setKeySetting(String category, String keyName, GSKeyCode keyCode) {
		synchronized (keySettings) {
			Map<String, GSKeyCode> categorySettings = keySettings.get(category);
			if (categorySettings == null) {
				categorySettings = new HashMap<>();
				keySettings.put(category, categorySettings);
			}
			
			categorySettings.put(keyName, keyCode);
		}
	}

	public GSKeyBinding registerKey(String name, String category, int keyCode, Runnable listener, GSEKeyEventType eventType) {
		return registerKey(name, category, keyCode, listener, eventType, true);
	}
	
	public GSKeyBinding registerKey(String name, String category, int keyCode, Runnable listener, GSEKeyEventType eventType, boolean allowDisabled) {
		return registerKey(name, category, GSKeyCode.fromType(InputUtil.Type.KEYSYM, keyCode), listener, eventType, allowDisabled);
	}
	
	public GSKeyBinding registerKey(String name, String category, GSKeyCode keyCode, Runnable listener, GSEKeyEventType eventType) {
		return registerKey(name, category, keyCode, listener, eventType, true);
	}

	public GSKeyBinding registerKey(String name, String category, GSKeyCode keyCode, Runnable listener, GSEKeyEventType eventType, boolean allowDisabled) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");
		
		return registerKeyImpl(name, category, keyCode, (key, type) -> {
			if (type == eventType)
				listener.run();
		}, allowDisabled);
	}

	public <T> GSKeyBinding registerKey(String name, String category, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		return registerKey(name, category, keyCode, listenerData, listener, eventType, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, int keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType, boolean allowDisabled) {
		return registerKey(name, category, GSKeyCode.fromType(InputUtil.Type.KEYSYM, keyCode), listenerData, listener, eventType, allowDisabled);
	}

	public <T> GSKeyBinding registerKey(String name, String category, GSKeyCode keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		return registerKey(name, category, keyCode, listenerData, listener, eventType, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, GSKeyCode keyCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType, boolean allowDisabled) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");
		
		return registerKeyImpl(name, category, keyCode, (key, type) -> {
			if (type == eventType)
				listener.accept(listenerData);
		}, allowDisabled);
	}
	
	public GSKeyBinding registerKey(String name, String category, GSKeyCode keyCode) {
		return registerKey(name, category, keyCode, true);
	}

	public GSKeyBinding registerKey(String name, String category, GSKeyCode keyCode, boolean allowDisabled) {
		return registerKeyImpl(name, category, keyCode, null, allowDisabled);
	}

	private GSKeyBinding registerKeyImpl(String name, String category, GSKeyCode keyCode, GSIKeyBindingListener listener, boolean allowDisabled) {
		if (name.contains(":") || category.contains(":"))
			throw new IllegalArgumentException("Invalid name or category! It must not contains ':'!");
		
		GSKeyBinding keyBinding = new GSKeyBinding(this, name, category, keyCode, allowDisabled);
		keyBinding.setKeyListener(listener);
		addKeyBinding(keyBinding);
		
		if (registerListener != null)
			registerListener.onKeyRegistered(keyBinding);
	
		return keyBinding;
	}
	
	private void addKeyBinding(GSKeyBinding keyBinding) {
		keyBindings.add(keyBinding);
		addKeyCodeMapping(keyBinding);
		
		GSKeyCode keyCodeSetting = getKeySetting(keyBinding.getCategory(), keyBinding.getName());
		if (keyCodeSetting != null) {
			keyBinding.setKeyCode(keyCodeSetting);
		} else {
			setKeySetting(keyBinding.getCategory(), keyBinding.getName(), keyBinding.getKeyCode());
		}
	}
	
	public void setKeyRegisterListener(GSIKeyBindingRegisterListener registerListener) {
		this.registerListener = registerListener;
	}

	private void handleKeyEvent(InputUtil.Key key, BiConsumer<GSKeyBinding, InputUtil.Key> eventMethod) {
		synchronized(codeToKeys) {
			List<GSKeyBinding> keyBindings = codeToKeys.get(key);
			if (keyBindings != null) {
				for (GSKeyBinding keyBinding : keyBindings)
					eventMethod.accept(keyBinding, key);
			}
		}
	}
	
	protected void onKeyCodeChanged(GSKeyBinding keyBinding, GSKeyCode oldKeyCode, GSKeyCode keyCode) {
		synchronized(codeToKeys) {
			for (int i = 0; i < oldKeyCode.getKeyCount(); i++) {
				InputUtil.Key key = oldKeyCode.get(i);

				List<GSKeyBinding> keysWithOldCode = codeToKeys.get(key);
				if (keysWithOldCode != null) {
					keysWithOldCode.remove(keyBinding);
					if (keysWithOldCode.isEmpty())
						codeToKeys.remove(key);
				}
			}
		}
		
		addKeyCodeMapping(keyBinding);
		
		setKeySetting(keyBinding.getCategory(), keyBinding.getName(), keyCode);
	}
	
	private void addKeyCodeMapping(GSKeyBinding keyBinding) {
		synchronized(codeToKeys) {
			GSKeyCode keyCode = keyBinding.getKeyCode();
			for (int i = 0; i < keyCode.getKeyCount(); i++) {
				InputUtil.Key key = keyCode.get(i);
				
				LinkedList<GSKeyBinding> keysWithCode = codeToKeys.get(key);
				if (keysWithCode == null) {
					keysWithCode = new LinkedList<>();
					codeToKeys.put(key, keysWithCode);
				}
				keysWithCode.add(keyBinding);
			}
		}
	}
	
	public void onKeyPressed(int key, int scancode, int mods) {
		handleKeyEvent(InputUtil.fromKeyCode(key, scancode), GSKeyBinding::onKeyPressed);
	}

	public void onKeyReleased(int key, int scancode, int mods) {
		handleKeyEvent(InputUtil.fromKeyCode(key, scancode), GSKeyBinding::onKeyReleased);
	}

	public void onMousePressed(int button, int mods) {
		handleKeyEvent(InputUtil.Type.MOUSE.createFromCode(button), GSKeyBinding::onKeyPressed);
	}

	public void onMouseReleased(int button, int mods) {
		handleKeyEvent(InputUtil.Type.MOUSE.createFromCode(button), GSKeyBinding::onKeyReleased);
	}
	
	public void clearEventQueue() {
		eventQueue.clear();
	}

	public void scheduleEvent(GSKeyBinding keyBinding) {
		eventQueue.add(keyBinding);
	}
	
	public void dispatchEvents(GSEKeyEventType eventType) {
		GSKeyBinding keyBinding;
		while ((keyBinding = eventQueue.poll()) != null)
			keyBinding.dispatchKeyEvent(eventType);
	}
	
	public List<GSKeyBinding> getKeyBindings() {
		return keyBindings;
	}
}
