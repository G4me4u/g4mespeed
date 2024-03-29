package com.g4mesoft.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.g4mesoft.setting.types.GSUnknownSetting;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;
import com.google.common.base.Predicates;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;

public final class GSSettingMap {

	public static final int MAX_TYPESTRING_LENGTH = 16;
	
	private final GSSettingCategory category;
	private final Map<String, GSSetting<?>> settings;
	
	private final GSSettingManager owner;
	
	public GSSettingMap(GSSettingCategory category, GSSettingManager owner) {
		this.category = category;
		this.owner = owner;
		
		settings = new LinkedHashMap<>();
	}
	
	public boolean isRegistered(String name) {
		return settings.containsKey(name);
	}
	
	public GSSetting<?> getSetting(String name) {
		return settings.get(name);
	}
	
	public void registerSetting(GSSetting<?> setting) {
		GSSetting<?> currentSetting = getSetting(setting.getName());
		if (currentSetting != null) {
			currentSetting.setSettingOwner(null);
			
			if (owner != null)
				owner.settingRemoved(category, currentSetting);
		}
		
		settings.put(setting.getName(), setting);
		setting.setSettingOwner(this);
		
		if (owner != null)
			owner.settingAdded(category, setting);

		// This has to be done at the end of the function
		// to ensure that the listeners will receive info
		// about the setting change, if one occurred.
		if (currentSetting != null)
			setValueFromLoadedSetting(setting, currentSetting);
	}
	
	private void setValueFromLoadedSetting(GSSetting<?> setting, GSSetting<?> loadedSetting) {
		if (setting.isSameSetting(loadedSetting)) {
			setting.setIfSameType(loadedSetting);
			// Do not update the is enabled in GUI setting from file.
			//setting.setEnabledInGui(loadedSetting.isEnabledInGui());
		} else {
			setting.reset();
		}
	}
	
	public void clearSettings() {
		for (GSSetting<?> setting : settings.values()) {
			setting.setSettingOwner(null);
			
			if (owner != null)
				owner.settingRemoved(category, setting);
		}
		settings.clear();
	}
	
	public GSSetting<?> removeSetting(String name) {
		GSSetting<?> currentSetting = settings.remove(name);
		if (currentSetting != null)
			owner.settingRemoved(category, currentSetting);
		return currentSetting;
	}
	
	public void resetSettings() {
		for (GSSetting<?> setting : settings.values())
			setting.reset();
	}
	
	
	public boolean isDisjoint(GSSettingMap other) {
		for (String name : settings.keySet()) {
			if (other.isRegistered(name))
				return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return settings.isEmpty();
	}
	
	public Collection<GSSetting<?>> getSettings() {
		return Collections.unmodifiableCollection(settings.values());
	}

	void settingChanged(GSSetting<?> setting) {
		if (owner != null)
			owner.settingChanged(category, setting);
	}

	public GSSettingCategory getCategory() {
		return category;
	}

	public void readSettings(GSDecodeBuffer buf) throws DecoderException {
		int remaining = buf.readInt();
		while (remaining-- > 0) {
			String name = buf.readString();
			String type = buf.readString(MAX_TYPESTRING_LENGTH);

			int sizeInBytes = buf.readInt();
			if (!buf.isReadable(sizeInBytes))
				throw new DecoderException("Not enough bytes in buffer!");
				
			int settingEnd = buf.getLocation() + sizeInBytes;
			
			GSSetting<?> setting;
			
			GSISettingDecoder<?> decoder = GSSettingManager.getDecoder(type);
			if (decoder == null) {
				byte[] data = new byte[sizeInBytes];
				buf.readBytes(data);
				setting = new GSUnknownSetting(name, type, data);
			} else {
				setting = decoder.decodeSetting(name, buf);

				int off = settingEnd - buf.getLocation();
				if (off > 0) {
					buf.skipBytes(off);
				} else {
					buf.setLocation(settingEnd);
				}
			}
			
			GSSetting<?> currentSetting = getSetting(setting.getName());
			if (currentSetting != null) {
				setValueFromLoadedSetting(currentSetting, setting);
			} else {
				// Setting was not added through the registerSetting
				// method and is therefore not necessarily active.
				setting.setActive(false);
				
				setting.setSettingOwner(this);
				settings.put(setting.getName(), setting);

				if (owner != null)
					owner.settingAdded(category, setting);
			}
		}
	}

	public void writeSettings(GSEncodeBuffer buf) throws DecoderException {
		writeSettings(buf, Predicates.alwaysTrue());
	}
	
	@SuppressWarnings("unchecked")
	public void writeSettings(GSEncodeBuffer buf, Predicate<GSSetting<?>> settingFilter) throws DecoderException {
		ByteBuf settingBuffer = Unpooled.buffer();
		
		List<GSSetting<?>> settingsToWrite = new ArrayList<GSSetting<?>>(settings.size());
		for (GSSetting<?> setting : settings.values()) {
			if (settingFilter.test(setting))
				settingsToWrite.add(setting);
		}
		
		buf.writeInt(settingsToWrite.size());
		for (GSSetting<?> setting : settingsToWrite) {
			buf.writeString(setting.getName());
			
			@SuppressWarnings("rawtypes")
			GSISettingDecoder decoder = GSSettingManager.getDecoder(setting);
			if (decoder == null) {
				if (setting instanceof GSUnknownSetting) {
					GSUnknownSetting unknSetting = ((GSUnknownSetting)setting);
					buf.writeString(unknSetting.getType(), MAX_TYPESTRING_LENGTH);
					buf.writeInt(unknSetting.getData().length);
					buf.writeBytes(unknSetting.getData());
				} else {
					buf.writeString(GSSettingManager.UNKNOWN_SETTING_TYPE);
					buf.writeInt(0);
				}
			} else {
				decoder.encodeSetting(GSEncodeBuffer.wrap(settingBuffer), setting);

				buf.writeString(decoder.getTypeString());
				buf.writeInt(settingBuffer.readableBytes());
				buf.writeBytes(settingBuffer);
				settingBuffer.clear();
			}
		}
		
		settingsToWrite.clear();
		settingBuffer.release(settingBuffer.refCnt());
	}
}
