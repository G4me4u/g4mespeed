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

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.g4mesoft.util.GSFileUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GSKeyManager {
	
	private final Map<String, Map<String, GSKeyCode>> keySettings;

	private final List<GSKeyBinding> keyBindings;
	private final Map<GSKey, LinkedList<GSKeyBinding>> codeToKeys;
	private final LinkedList<GSKeyBinding> eventQueue;
	private int queuePriority;

	private GSIKeyBindingRegisterListener registerListener;
	
	public GSKeyManager() {
		keySettings = new HashMap<>();

		keyBindings = new ArrayList<>();
		codeToKeys = new HashMap<>();
		eventQueue = new LinkedList<>();
		queuePriority = 0;
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
				
				GSKey[] keys = new GSKey[keyArgs.length];
				for (int i = 0; i < keyArgs.length; i++) {
					try {
						keys[i] = GSKey.fromName(keyArgs[i]);
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
							bw.write(keyCode.get(i).getName());
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

	public GSKeyBinding registerKey(String name, String category, GSKey key, Runnable listener, GSEKeyEventType eventType) {
		return registerKey(name, category, key, listener, eventType, true);
	}
	
	public GSKeyBinding registerKey(String name, String category, GSKey key, Runnable listener, GSEKeyEventType eventType, boolean allowDisabled) {
		return registerKey(name, category, GSKeyCode.fromKey(key), listener, eventType, allowDisabled);
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

	public <T> GSKeyBinding registerKey(String name, String category, GSKey key, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		return registerKey(name, category, key, listenerData, listener, eventType, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, GSKey key, T listenerData, Consumer<T> listener, GSEKeyEventType eventType, boolean allowDisabled) {
		return registerKey(name, category, GSKeyCode.fromKey(key), listenerData, listener, eventType, allowDisabled);
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
		
		GSKeyBinding keyBinding = new GSKeyBinding(this, name, category, keyCode, allowDisabled, 0);
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

	private void handleKeyEvent(GSKey key, BiConsumer<GSKeyBinding, GSKey> eventMethod) {
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
				GSKey key = oldKeyCode.get(i);

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
				GSKey key = keyCode.get(i);
				
				LinkedList<GSKeyBinding> keysWithCode = codeToKeys.get(key);
				if (keysWithCode == null) {
					keysWithCode = new LinkedList<>();
					codeToKeys.put(key, keysWithCode);
				}
				keysWithCode.add(keyBinding);
			}
		}
	}
	
	public void onKeyPressed(int keyCode) {
		handleKeyEvent(GSKey.fromKeyCode(GSEKeyType.KEYBOARD, keyCode), GSKeyBinding::onKeyPressed);
	}

	public void onKeyReleased(int keyCode) {
		handleKeyEvent(GSKey.fromKeyCode(GSEKeyType.KEYBOARD, keyCode), GSKeyBinding::onKeyReleased);
	}

	public void onMousePressed(int button) {
		handleKeyEvent(GSKey.fromKeyCode(GSEKeyType.MOUSE, button), GSKeyBinding::onKeyPressed);
	}

	public void onMouseReleased(int button) {
		handleKeyEvent(GSKey.fromKeyCode(GSEKeyType.MOUSE, button), GSKeyBinding::onKeyReleased);
	}
	
	public void clearEventQueue() {
		eventQueue.clear();
		queuePriority = 0;
	}

	public void handleKeyboard() {
		clearEventQueue();
		
		int key = Keyboard.getEventKey();
		if (key != Keyboard.KEY_NONE) {
			if (Keyboard.getEventKeyState()) {
				onKeyPressed(key);
			} else {
				onKeyReleased(key);
			}
		}
	}
	
	public void handleMouse() {
		clearEventQueue();
		if (Mouse.getEventButtonState()) {
			onMousePressed(Mouse.getEventButton());
		} else {
			onMouseReleased(Mouse.getEventButton());
		}
	}
	
	public void scheduleEvent(GSKeyBinding keyBinding) {
		int priority = keyBinding.getPriority();
		if (priority >= queuePriority) {
			// We have a priority greater than every other key.
			// Clear the queue to ensure key is dominant.
			if (priority > queuePriority)
				eventQueue.clear();

			eventQueue.add(keyBinding);
			queuePriority = priority;
		}
	}
	
	public void dispatchEvents(GSEKeyEventType eventType) {
		GSKeyBinding keyBinding;
		while ((keyBinding = eventQueue.poll()) != null)
			keyBinding.dispatchKeyEvent(eventType);
		clearEventQueue();
	}
	
	public List<GSKeyBinding> getKeyBindings() {
		return keyBindings;
	}
}