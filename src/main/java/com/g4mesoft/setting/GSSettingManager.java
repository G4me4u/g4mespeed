package com.g4mesoft.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.g4mesoft.core.GSController;
import com.g4mesoft.setting.decoder.GSBooleanSettingDecoder;
import com.g4mesoft.setting.decoder.GSFloatSettingDecoder;
import com.g4mesoft.setting.decoder.GSISettingDecoder;
import com.g4mesoft.setting.decoder.GSIntegerSettingDecoder;
import com.g4mesoft.setting.decoder.GSStringSettingDecoder;
import com.g4mesoft.setting.types.GSUnknownSetting;

import io.netty.buffer.Unpooled;
import net.minecraft.util.PacketByteBuf;

public class GSSettingManager {

	private static final String SETTINGS_PATH = "settings.cfg";
	static final String UNKNOWN_SETTING_TYPE = "UNKN";
	
	private static final byte CATEGORY_ENTRY_CODE = 0x55;
	
	private final GSController controller;
	
	private final Map<GSSettingCategory, GSSettingMap> settings;
	private final List<GSISettingChangeListener> listeners;
	
	private final Map<String, GSISettingDecoder<?>> typeToDecoder;
	@SuppressWarnings("rawtypes")
	private Map<Class<? extends GSSetting>, GSISettingDecoder> clazzToDecoder;
	
	public GSSettingManager(GSController controller) {
		this.controller = controller;
		
		settings = new HashMap<GSSettingCategory, GSSettingMap>();
		listeners = new ArrayList<GSISettingChangeListener>();
		
		typeToDecoder = new HashMap<String, GSISettingDecoder<?>>();
		
		@SuppressWarnings("rawtypes")
		Map<Class<? extends GSSetting>, GSISettingDecoder> tmpClazzToDecoder = 
			new HashMap<Class<? extends GSSetting>, GSISettingDecoder>();
		clazzToDecoder = tmpClazzToDecoder;
	
		registerDefaultParsers();
	}
	
	private void registerDefaultParsers() {
		registerSettingDecoder(new GSBooleanSettingDecoder());
		registerSettingDecoder(new GSFloatSettingDecoder());
		registerSettingDecoder(new GSIntegerSettingDecoder());
		registerSettingDecoder(new GSStringSettingDecoder());
	}
	
	public void loadSettings() {
		try (FileInputStream is = new FileInputStream(getSettingsFile())) {
			readSettings(is);
		} catch (IOException e) {
		}
	}
	
	public void saveSettings() {
		try {
			File settingFile = getSettingsFile();
			if (!settingFile.isFile()) {
				File parentSettingFile = settingFile.getParentFile();
				if (parentSettingFile != null && !parentSettingFile.exists())
					parentSettingFile.mkdirs();
				settingFile.createNewFile();
			}
			
			try (FileOutputStream os = new FileOutputStream(settingFile)) {
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
				GSSettingCategory category = new GSSettingCategory(buffer.readString());
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
	
	private File getSettingsFile() {
		return new File(controller.getCacheFile(), SETTINGS_PATH);
	}

	public void registerSettingDecoder(GSISettingDecoder<?> decoder) {
		String type = decoder.getTypeString();
		@SuppressWarnings("rawtypes")
		Class<? extends GSSetting> clazz = decoder.getSettingClass();
		
		if (typeToDecoder.containsKey(type))
			throw new RuntimeException("A decoder with type-name \"" + type + "\" already exists.");
		if (clazzToDecoder.containsKey(clazz))
			throw new RuntimeException("A decoder for setting class \"" + clazz + "\" already exists.");
		
		typeToDecoder.put(type, decoder);
		clazzToDecoder.put(clazz, decoder);
	}
	
	GSISettingDecoder<?> getSettingDecoder(String type) {
		if (UNKNOWN_SETTING_TYPE.equals(type))
			return null;
		return typeToDecoder.get(type);
	}
	
	@SuppressWarnings("rawtypes")
	GSISettingDecoder<?> getSettingDecoder(Class<? extends GSSetting> clazz) {
		if (GSUnknownSetting.class.equals(clazz))
			return null;
		return clazzToDecoder.get(clazz);
	}
	
	public GSSetting<?> getSetting(GSSettingCategory category, String name) {
		GSSettingMap categorySettings = settings.get(category);
		if (categorySettings == null)
			return null;
		return categorySettings.getSetting(name);
	}
	
	public void registerSetting(GSSettingCategory category, GSSetting<?> setting) {
		GSSettingMap categorySettings = settings.get(category);
		if (categorySettings == null) {
			categorySettings = new GSSettingMap(category, this);
			settings.put(category, categorySettings);
		}
		
		categorySettings.registerSetting(setting);
		
		for (GSISettingChangeListener listener : listeners)
			listener.onSettingAdded(category, setting);
	}
	
	public Collection<GSSettingMap> getSettings() {
		return Collections.unmodifiableCollection(settings.values());
	}
	
	void settingChanged(GSSettingCategory category, GSSetting<?> setting) {
		for (GSISettingChangeListener listener : listeners)
			listener.onSettingChanged(category, setting);
	}

	public void addChangeListener(GSISettingChangeListener changeListener) {
		listeners.add(changeListener);
	}

	public void removeChangeListener(GSISettingChangeListener changeListener) {
		listeners.remove(changeListener);
	}
}
