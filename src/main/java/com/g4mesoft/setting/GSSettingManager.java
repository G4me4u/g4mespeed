package com.g4mesoft.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.g4mesoft.setting.decoder.GSBooleanSettingDecoder;
import com.g4mesoft.setting.decoder.GSFloatSettingDecoder;
import com.g4mesoft.setting.decoder.GSISettingDecoder;
import com.g4mesoft.setting.decoder.GSIntegerSettingDecoder;
import com.g4mesoft.setting.decoder.GSStringSettingDecoder;
import com.g4mesoft.setting.types.GSUnknownSetting;
import com.g4mesoft.util.GSBufferUtil;
import com.g4mesoft.util.GSFileUtils;

import io.netty.buffer.Unpooled;
import net.minecraft.util.PacketByteBuf;

public class GSSettingManager {

	static final String UNKNOWN_SETTING_TYPE = "UNKN";
	
	private static final byte CATEGORY_ENTRY_CODE = 0x55;
	
	protected final Map<GSSettingCategory, GSSettingMap> settings;
	private final List<GSISettingChangeListener> listeners;
	
	private static final Map<String, GSISettingDecoder<?>> typeToDecoder;
	@SuppressWarnings("rawtypes")
	private static final Map<Class<? extends GSSetting>, GSISettingDecoder> clazzToDecoder;
	
	static {
		typeToDecoder = new HashMap<String, GSISettingDecoder<?>>();
		
		@SuppressWarnings("rawtypes")
		Map<Class<? extends GSSetting>, GSISettingDecoder> tmpClazzToDecoder = 
			new HashMap<Class<? extends GSSetting>, GSISettingDecoder>();
		clazzToDecoder = tmpClazzToDecoder;

		registerDefaultParsers();
	}
	
	public GSSettingManager() {
		settings = new LinkedHashMap<GSSettingCategory, GSSettingMap>();
		listeners = new ArrayList<GSISettingChangeListener>();
	}
	
	private static void registerDefaultParsers() {
		registerSettingDecoder(new GSBooleanSettingDecoder());
		registerSettingDecoder(new GSFloatSettingDecoder());
		registerSettingDecoder(new GSIntegerSettingDecoder());
		registerSettingDecoder(new GSStringSettingDecoder());
	}

	public static synchronized void registerSettingDecoder(GSISettingDecoder<?> decoder) {
		String type = decoder.getTypeString();
		@SuppressWarnings("rawtypes")
		Class<? extends GSSetting> clazz = decoder.getSettingClass();
		
		if (typeToDecoder.containsKey(type))
			throw new RuntimeException("A decoder with type-name \"" + type + "\" already exists.");
		if (clazzToDecoder.containsKey(clazz))
			throw new RuntimeException("A decoder for setting class \"" + clazz + "\" already exists.");
		
		synchronized (typeToDecoder) {
			typeToDecoder.put(type, decoder);
		}

		synchronized (clazzToDecoder) {
			clazzToDecoder.put(clazz, decoder);
		}
	}
	
	public static GSISettingDecoder<?> getSettingDecoder(String type) {
		if (UNKNOWN_SETTING_TYPE.equals(type))
			return null;

		synchronized (typeToDecoder) {
			return typeToDecoder.get(type);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static GSISettingDecoder<?> getSettingDecoder(Class<? extends GSSetting> clazz) {
		if (GSUnknownSetting.class.equals(clazz))
			return null;
		
		synchronized (clazzToDecoder) {
			return clazzToDecoder.get(clazz);
		}
	}
	
	public void loadSettings(File settingsFile) {
		try (FileInputStream is = new FileInputStream(settingsFile)) {
			readSettings(is);
		} catch (IOException e) {
		}
	}
	
	public void saveSettings(File settingsFile) {
		try {
			GSFileUtils.ensureFileExists(settingsFile);
			
			try (FileOutputStream os = new FileOutputStream(settingsFile)) {
				writeSettings(os);
			}
		} catch (IOException e) {
		}
	}
	
	private void readSettings(FileInputStream is) throws IOException {
		byte[] data = IOUtils.toByteArray(is);
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.wrappedBuffer(data));
		readSettings(buffer);
		buffer.release();
	}

	private void writeSettings(FileOutputStream os) throws IOException {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		writeSettings(buffer);
		if (buffer.hasArray()) {
			os.write(buffer.array(), buffer.arrayOffset(), buffer.writerIndex());
		} else {
			os.getChannel().write(buffer.nioBuffer());
		}
		buffer.release();
	}
	
	public void readSettings(PacketByteBuf buffer) throws IOException {
		while (buffer.isReadable()) {
			if (buffer.readByte() == CATEGORY_ENTRY_CODE) {
				GSSettingCategory category = new GSSettingCategory(buffer.readString(GSBufferUtil.MAX_STRING_LENGTH));
				GSSettingMap map = settings.get(category);
				
				if (map == null) {
					map = new GSSettingMap(category, this);
					settings.put(category, map);
				}
				
				try {
					map.readSettings(buffer);
				} catch (Exception e) {
				}
			}
		}
	}
	
	public void writeSettings(PacketByteBuf buffer) throws IOException {
		for (GSSettingMap map : settings.values()) {
			GSSettingCategory category = map.getCategory();
			buffer.writeByte(CATEGORY_ENTRY_CODE);
			buffer.writeString(category.getName());
			
			try {
				map.writeSettings(buffer);
			} catch (Exception e) {
			}
		}
	}
	
	public GSSetting<?> getSetting(GSSettingCategory category, String name) {
		GSSettingMap categorySettings = settings.get(category);
		return (categorySettings != null) ? categorySettings.getSetting(name) : null;
	}
	
	public void registerSetting(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingMap categorySettings = settings.get(category);
		if (categorySettings == null) {
			categorySettings = new GSSettingMap(category, this);
			settings.put(category, categorySettings);
		}
		
		categorySettings.registerSetting(setting);
	}
	
	public void removeSetting(GSSettingCategory category, String name) {
		GSSettingMap categorySettings = settings.get(category);
		if (categorySettings != null)
			categorySettings.removeSetting(name);
	}
	
	public Collection<GSSettingMap> getSettings() {
		return Collections.unmodifiableCollection(settings.values());
	}

	public boolean hasCategory(GSSettingCategory category) {
		return settings.containsKey(category);
	}
	
	public void clearSettings() {
		for (GSSettingMap settingMap : settings.values())
			settingMap.clearSettings();
		settings.clear();
	}
	
	void settingChanged(GSSettingCategory category, GSSetting<?> setting) {
		for (GSISettingChangeListener listener : listeners)
			listener.onSettingChanged(category, setting);
	}
	
	void settingAdded(GSSettingCategory category, GSSetting<?> setting) {
		for (GSISettingChangeListener listener : listeners)
			listener.onSettingAdded(category, setting);
	}

	void settingRemoved(GSSettingCategory category, GSSetting<?> setting) {
		for (GSISettingChangeListener listener : listeners)
			listener.onSettingRemoved(category, setting);
	}

	public void addChangeListener(GSISettingChangeListener changeListener) {
		listeners.add(changeListener);
	}

	public void removeChangeListener(GSISettingChangeListener changeListener) {
		listeners.remove(changeListener);
	}
}
