package com.g4mesoft.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.g4mesoft.setting.decoder.GSISettingDecoder;
import com.g4mesoft.setting.types.GSUnknownSetting;
import com.google.common.base.Predicates;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.util.PacketByteBuf;

public final class GSSettingMap {

	private final GSSettingCategory category;
	private final Map<String, GSSetting<?>> settings;
	
	private final GSSettingManager owner;
	
	public GSSettingMap(GSSettingCategory category, GSSettingManager owner) {
		this.category = category;
		this.owner = owner;
		
		settings = new LinkedHashMap<String, GSSetting<?>>();
	}
	
	public GSSetting<?> getSetting(String name) {
		return settings.get(name);
	}
	
	public void registerSetting(GSSetting<?> setting) {
		GSSetting<?> currentSetting = getSetting(setting.getName());
		if (currentSetting != null) {
			setting.setValueIfSameType(currentSetting);
			currentSetting.setSettingOwner(null);
			
			if (owner != null)
				owner.settingRemoved(category, currentSetting);
		}
		
		settings.put(setting.getName(), setting);
		setting.setSettingOwner(this);
		
		if (owner != null)
			owner.settingAdded(category, setting);
	}
	
	public void clearSettings() {
		for (GSSetting<?> setting : settings.values()) {
			setting.setSettingOwner(null);
			
			if (owner != null)
				owner.settingRemoved(category, setting);
		}
	}
	
	public GSSetting<?> removeSetting(String name) {
		GSSetting<?> currentSetting = settings.remove(name);
		if (currentSetting != null)
			owner.settingRemoved(category, currentSetting);
		return currentSetting;
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

	public void readSettings(PacketByteBuf buffer) throws DecoderException {
		int remaining = buffer.readInt();
		while (remaining-- > 0) {
			String name = buffer.readString(32767);
			String type = buffer.readString(16);
			int sizeInBytes = buffer.readInt();

			if (buffer.readableBytes() < sizeInBytes)
				throw new DecoderException("Not enough bytes in buffer!");
				
			int settingEnd = buffer.readerIndex() + sizeInBytes;
			
			GSSetting<?> setting;
			
			GSISettingDecoder<?> decoder = GSSettingManager.getSettingDecoder(type);
			if (decoder == null) {
				byte[] data = new byte[sizeInBytes];
				buffer.readBytes(data);
				setting = new GSUnknownSetting(name, type, data);
			} else {
				setting = decoder.decodeSetting(name, buffer);

				int off = settingEnd - buffer.readerIndex();
				if (off > 0) {
					buffer.skipBytes(off);
				} else {
					buffer.readerIndex(settingEnd);
				}
			}
			
			GSSetting<?> currentSetting = getSetting(setting.getName());
			if (currentSetting != null) {
				currentSetting.setValueIfSameType(setting);
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

	public void writeSettings(PacketByteBuf buffer) throws DecoderException {
		writeSettings(buffer, Predicates.alwaysTrue());
	}
	
	@SuppressWarnings("unchecked")
	public void writeSettings(PacketByteBuf buffer, Predicate<GSSetting<?>> settingFilter) throws DecoderException {
		PacketByteBuf settingBuffer = new PacketByteBuf(Unpooled.buffer());
		
		List<GSSetting<?>> settingsToWrite = new ArrayList<GSSetting<?>>(settings.size());
		for (GSSetting<?> setting : settings.values()) {
			if (settingFilter.test(setting))
				settingsToWrite.add(setting);
		}
		
		buffer.writeInt(settingsToWrite.size());
		for (GSSetting<?> setting : settingsToWrite) {
			buffer.writeString(setting.getName());
			
			@SuppressWarnings("rawtypes")
			GSISettingDecoder decoder = GSSettingManager.getSettingDecoder(setting.getClass());
			if (decoder == null) {
				if (setting instanceof GSUnknownSetting) {
					GSUnknownSetting unknSetting = ((GSUnknownSetting)setting);
					buffer.writeString(unknSetting.getType());
					buffer.writeInt(unknSetting.getData().length);
					buffer.writeBytes(unknSetting.getData());
				} else {
					buffer.writeString(GSSettingManager.UNKNOWN_SETTING_TYPE);
					buffer.writeInt(0);
				}
			} else {
				decoder.encodeSetting(settingBuffer, setting);

				buffer.writeString(decoder.getTypeString());
				buffer.writeInt(settingBuffer.writerIndex() - settingBuffer.readerIndex());
				buffer.writeBytes(settingBuffer);
				settingBuffer.clear();
			}
		}
		
		settingsToWrite.clear();
		settingBuffer.release(settingBuffer.refCnt());
	}
}
