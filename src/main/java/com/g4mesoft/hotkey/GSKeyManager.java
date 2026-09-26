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
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;

@Environment(EnvType.CLIENT)
public class GSKeyManager {
	
	private final Map<String, Map<String, GSMultiKey>> keySettings;

	private final List<GSKeyBinding> keyBindings;
	private final Map<InputConstants.Key, LinkedList<GSKeyBinding>> codeToKeys;
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
				
				InputConstants.Key[] keys = new InputConstants.Key[keyArgs.length];
				for (int i = 0; i < keyArgs.length; i++) {
					try {
						keys[i] = InputConstants.getKey(keyArgs[i]);
					} catch (IllegalArgumentException e) {
						continue outer;
					}
				}
				
				setKeySetting(args[0], args[1], GSMultiKey.fromKeys(keys));
			}
		} catch (IOException e) {
		}
	}

	public void saveKeys(File keySettingsFile) {
		try {
			GSFileUtil.ensureFileExists(keySettingsFile);
			
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(keySettingsFile))) {
				for (Map.Entry<String, Map<String, GSMultiKey>> categorySettings : keySettings.entrySet()) {
					String category = categorySettings.getKey();
					for (Map.Entry<String, GSMultiKey> setting : categorySettings.getValue().entrySet()) {
						bw.write(category);
						bw.write(':');
						bw.write(setting.getKey());
						bw.write(':');
						GSMultiKey multiKey = setting.getValue();
						for (int i = 0; i < multiKey.getKeyCount(); i++) {
							if (i != 0)
								bw.write(',');
							bw.write(multiKey.get(i).getName());
						}
						bw.newLine();
					}
				}
			}
		} catch (IOException e) {
		}
	}
	
	private GSMultiKey getKeySetting(String category, String keyName) {
		synchronized (keySettings) {
			Map<String, GSMultiKey> categorySettings = keySettings.get(category);
			return (categorySettings == null) ? null : categorySettings.get(keyName);
		}
	}

	private void setKeySetting(String category, String keyName, GSMultiKey multiKey) {
		synchronized (keySettings) {
			Map<String, GSMultiKey> categorySettings = keySettings.get(category);
			if (categorySettings == null) {
				categorySettings = new HashMap<>();
				keySettings.put(category, categorySettings);
			}
			
			categorySettings.put(keyName, multiKey);
		}
	}

	public GSKeyBinding registerKey(String name, String category, int scanCode, Runnable listener, GSEKeyEventType eventType) {
		return registerKey(name, category, scanCode, listener, eventType, true);
	}
	
	public GSKeyBinding registerKey(String name, String category, int scanCode, Runnable listener, GSEKeyEventType eventType, boolean allowDisabled) {
		return registerKey(name, category, GSMultiKey.fromType(InputConstants.Type.KEYBOARD, scanCode), listener, eventType, allowDisabled);
	}
	
	public GSKeyBinding registerKey(String name, String category, GSMultiKey multiKey, Runnable listener, GSEKeyEventType eventType) {
		return registerKey(name, category, multiKey, listener, eventType, true);
	}

	public GSKeyBinding registerKey(String name, String category, GSMultiKey multiKey, Runnable listener, GSEKeyEventType eventType, boolean allowDisabled) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");
		
		return registerKeyImpl(name, category, multiKey, (_, type) -> {
			if (type == eventType)
				listener.run();
		}, allowDisabled);
	}

	public <T> GSKeyBinding registerKey(String name, String category, int scanCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		return registerKey(name, category, scanCode, listenerData, listener, eventType, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, int scanCode, T listenerData, Consumer<T> listener, GSEKeyEventType eventType, boolean allowDisabled) {
		return registerKey(name, category, GSMultiKey.fromType(InputConstants.Type.KEYBOARD, scanCode), listenerData, listener, eventType, allowDisabled);
	}

	public <T> GSKeyBinding registerKey(String name, String category, GSMultiKey multiKey, T listenerData, Consumer<T> listener, GSEKeyEventType eventType) {
		return registerKey(name, category, multiKey, listenerData, listener, eventType, true);
	}

	public <T> GSKeyBinding registerKey(String name, String category, GSMultiKey multiKey, T listenerData, Consumer<T> listener, GSEKeyEventType eventType, boolean allowDisabled) {
		if (listener == null)
			throw new IllegalArgumentException("Listener is null");
		
		return registerKeyImpl(name, category, multiKey, (_, type) -> {
			if (type == eventType)
				listener.accept(listenerData);
		}, allowDisabled);
	}
	
	public GSKeyBinding registerKey(String name, String category, GSMultiKey multiKey) {
		return registerKey(name, category, multiKey, true);
	}

	public GSKeyBinding registerKey(String name, String category, GSMultiKey multiKey, boolean allowDisabled) {
		return registerKeyImpl(name, category, multiKey, null, allowDisabled);
	}

	private GSKeyBinding registerKeyImpl(String name, String category, GSMultiKey multiKey, GSIKeyBindingListener listener, boolean allowDisabled) {
		if (name.contains(":") || category.contains(":"))
			throw new IllegalArgumentException("Invalid name or category! It must not contains ':'!");
		
		GSKeyBinding keyBinding = new GSKeyBinding(this, name, category, multiKey, allowDisabled, 0);
		keyBinding.setKeyListener(listener);
		addKeyBinding(keyBinding);
		
		if (registerListener != null)
			registerListener.onKeyRegistered(keyBinding);
	
		return keyBinding;
	}
	
	private void addKeyBinding(GSKeyBinding keyBinding) {
		keyBindings.add(keyBinding);
		addKeyMapping(keyBinding);
		
		GSMultiKey multiKeySetting = getKeySetting(keyBinding.getCategory(), keyBinding.getName());
		if (multiKeySetting != null) {
			keyBinding.setKey(multiKeySetting);
		} else {
			setKeySetting(keyBinding.getCategory(), keyBinding.getName(), keyBinding.getKey());
		}
	}
	
	public void setKeyRegisterListener(GSIKeyBindingRegisterListener registerListener) {
		this.registerListener = registerListener;
	}

	private void handleKeyEvent(InputConstants.Key key, BiConsumer<GSKeyBinding, InputConstants.Key> eventMethod) {
		synchronized(codeToKeys) {
			List<GSKeyBinding> keyBindings = codeToKeys.get(key);
			if (keyBindings != null) {
				for (GSKeyBinding keyBinding : keyBindings)
					eventMethod.accept(keyBinding, key);
			}
		}
	}
	
	protected void onKeyChanged(GSKeyBinding keyBinding, GSMultiKey oldMultiKey, GSMultiKey multiKey) {
		synchronized(codeToKeys) {
			for (int i = 0; i < oldMultiKey.getKeyCount(); i++) {
				InputConstants.Key key = oldMultiKey.get(i);

				List<GSKeyBinding> keysWithOldCode = codeToKeys.get(key);
				if (keysWithOldCode != null) {
					keysWithOldCode.remove(keyBinding);
					if (keysWithOldCode.isEmpty())
						codeToKeys.remove(key);
				}
			}
		}
		
		addKeyMapping(keyBinding);
		
		setKeySetting(keyBinding.getCategory(), keyBinding.getName(), multiKey);
	}
	
	private void addKeyMapping(GSKeyBinding keyBinding) {
		synchronized(codeToKeys) {
			GSMultiKey multiKey = keyBinding.getKey();
			for (int i = 0; i < multiKey.getKeyCount(); i++) {
				InputConstants.Key key = multiKey.get(i);
				
				LinkedList<GSKeyBinding> keysWithCode = codeToKeys.get(key);
				if (keysWithCode == null) {
					keysWithCode = new LinkedList<>();
					codeToKeys.put(key, keysWithCode);
				}
				keysWithCode.add(keyBinding);
			}
		}
	}
	
	public void onKeyPressed(KeyEvent input) {
		handleKeyEvent(InputConstants.getKey(input), GSKeyBinding::onKeyPressed);
	}

	public void onKeyReleased(KeyEvent input) {
		handleKeyEvent(InputConstants.getKey(input), GSKeyBinding::onKeyReleased);
	}

	public void onMousePressed(MouseButtonInfo input) {
		handleKeyEvent(InputConstants.Type.MOUSE.getOrCreate(input.button()), GSKeyBinding::onKeyPressed);
	}

	public void onMouseReleased(MouseButtonInfo input) {
		handleKeyEvent(InputConstants.Type.MOUSE.getOrCreate(input.button()), GSKeyBinding::onKeyReleased);
	}
	
	public void clearEventQueue() {
		eventQueue.clear();
		queuePriority = 0;
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
