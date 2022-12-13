package com.g4mesoft.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.g4mesoft.gui.setting.GSBooleanSettingPanel;
import com.g4mesoft.gui.setting.GSFloatSettingPanel;
import com.g4mesoft.gui.setting.GSIntegerSettingPanel;
import com.g4mesoft.setting.decoder.GSBooleanSettingDecoder;
import com.g4mesoft.setting.decoder.GSFloatSettingDecoder;
import com.g4mesoft.setting.decoder.GSIntegerSettingDecoder;
import com.g4mesoft.setting.decoder.GSStringSettingDecoder;
import com.g4mesoft.setting.types.GSUnknownSetting;
import com.g4mesoft.util.GSBufferUtil;
import com.g4mesoft.util.GSFileUtil;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class GSSettingManager {

	static final String UNKNOWN_SETTING_TYPE = "UNKN";
	
	private static final byte CATEGORY_ENTRY_CODE = 0x55;
	
	protected final Map<GSSettingCategory, GSSettingMap> settings;
	private final List<GSISettingChangeListener> listeners;
	
	private static final Map<String, GSISettingDecoder<?>> typeToDecoder;
	private static final Map<Class<? extends GSSetting<?>>, GSISettingDecoder<?>> clazzToDecoder;
	private static final Map<Class<? extends GSSetting<?>>, GSISettingPanelSupplier<?>> panelSuppliers;
	
	static {
		// Ensure synchronized since they might be called from network thread.
		typeToDecoder = Collections.synchronizedMap(new HashMap<>());
		clazzToDecoder = Collections.synchronizedMap(new IdentityHashMap<>());
		panelSuppliers = Collections.synchronizedMap(new HashMap<>());
		
		registerDefaultTypes();
	}
	
	public GSSettingManager() {
		settings = new LinkedHashMap<>();
		listeners = new ArrayList<>();
	}
	
	private static void registerDefaultTypes() {
		registerType(new GSBooleanSettingDecoder(), GSBooleanSettingPanel::new);
		registerType(new GSFloatSettingDecoder(), GSFloatSettingPanel::new);
		registerType(new GSIntegerSettingDecoder(), GSIntegerSettingPanel::new);
		// TODO(Christian): implement setting panel for strings.
		registerDecoder(new GSStringSettingDecoder());
	}
	
	/**
	 * Registers both a decoder/encoder and a panel supplier for a custom setting type.
	 * The decoder will be used during reading/writing to files or for transmitting settings
	 * over the network (in setting update packets). The panel supplier is used to create
	 * appropriate setting UI elements in the settings GUI. If one does not wish to register
	 * a panel supplier for the given setting type, {@link #registerDecoder(GSISettingDecoder)}
	 * should be used instead.
	 * 
	 * @param <T> - The setting type to register
	 * @param decoder - The decoder to be registered
	 * @param panelSupplier - The panel supplier to be registered
	 * 
	 * @throws NullPointerException if either {@code decoder.getTypeString()} or
	 *                              {@code decoder.getSettingClass()} returns null.
	 * @throws RuntimeException if a decoder is already specified with the decoder
	 *                          setting class, or the decoder type string is already
	 *                          in use.
	 * 
	 * @see #registerDecoder(GSISettingDecoder)
	 */
	public static <T extends GSSetting<?>> void registerType(GSISettingDecoder<T> decoder,
	                                                         GSISettingPanelSupplier<T> panelSupplier) {
		if (panelSupplier == null)
			throw new IllegalArgumentException("panelSupplier is null");
		registerDecoder(decoder);
		// Register panel supplier (always succeeds since decoder registered successfully).
		panelSuppliers.put(decoder.getSettingClass(), panelSupplier);
	}
	
	/**
	 * Registers a decoder/encoder for a custom setting type. If one wishes to also
	 * register a custom UI element for the particular setting type, then use the
	 * {@link #registerType(GSISettingDecoder, GSISettingPanelSupplier)} method instead.
	 * 
	 * @param <T> - The setting type to register this decoder for
	 * @param decoder - The decoder to be registered
	 * 
	 * @throws NullPointerException if either {@code decoder.getTypeString()} or
	 *                              {@code decoder.getSettingClass()} returns null.
	 * @throws RuntimeException if a decoder is already specified with the decoder
	 *                          setting class, or the decoder type string is already
	 *                          in use.
	 * 
	 * @see #registerType(GSISettingDecoder, GSISettingPanelSupplier)
	 */
	public static <T extends GSSetting<?>> void registerDecoder(GSISettingDecoder<T> decoder) {
		String type = decoder.getTypeString();
		Class<T> clazz = decoder.getSettingClass();

		if (type == null || clazz == null)
			throw new NullPointerException("Decoder type-name or setting class is null");
		if (UNKNOWN_SETTING_TYPE.equals(type) || typeToDecoder.containsKey(type))
			throw new RuntimeException("A decoder with type-name \"" + type + "\" already exists.");
		if (clazz == GSUnknownSetting.class || clazzToDecoder.containsKey(clazz))
			throw new RuntimeException("A decoder for setting class \"" + clazz + "\" already exists.");
		
		typeToDecoder.put(type, decoder);
		clazzToDecoder.put(clazz, decoder);
	}
	
	public static GSISettingDecoder<?> getDecoder(String type) {
		return typeToDecoder.get(type);
	}
	
	public static <T extends GSSetting<?>> GSISettingDecoder<T> getDecoder(T setting) {
		// Note: unsafe casting works in this case, since we at
		//       worst down-cast the actual type of the setting.
		@SuppressWarnings("unchecked")
		GSISettingDecoder<T> decoder =
			(GSISettingDecoder<T>)clazzToDecoder.get(setting.getClass());
		return decoder;
	}
	
	public static <T extends GSSetting<?>> GSISettingPanelSupplier<T> getPanelSupplier(T setting) {
		// See note above for unsafe casting.
		@SuppressWarnings("unchecked")
		GSISettingPanelSupplier<T> supplier =
			(GSISettingPanelSupplier<T>)panelSuppliers.get(setting.getClass());
		return supplier;
	}
	
	/* Methods for reading and writing settings */
	
	public void loadSettings(File settingsFile) {
		try (FileInputStream is = new FileInputStream(settingsFile)) {
			readSettings(is);
		} catch (IOException e) {
		}
	}
	
	public void saveSettings(File settingsFile) {
		try {
			GSFileUtil.ensureFileExists(settingsFile);
			
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
	
	/* Methods for registering and querying settings */
	
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
	
	public void resetSettings() {
		for (GSSettingMap settingMap : settings.values())
			settingMap.resetSettings();
	}
	
	/* Methods for dispatching events */
	
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
