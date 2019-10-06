package com.g4mesoft.setting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.setting.decoder.GSISettingDecoder;
import com.g4mesoft.setting.types.GSUnknownSetting;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.util.PacketByteBuf;

public class GSSettingMap {

	private final GSSettingCategory category;
	private final GSSettingManager owner;
	
	private final Map<String, GSSetting<?>> settings;
	
	public GSSettingMap(GSSettingCategory category, GSSettingManager owner) {
		this.category = category;
		this.owner = owner;
		
		settings = new HashMap<String, GSSetting<?>>();
	}
	
	public GSSetting<?> getSetting(String name) {
		return settings.get(name);
	}
	
	public void registerSetting(GSSetting<?> setting) {
		GSSetting<?> currentSetting = getSetting(setting.getName());
		if (currentSetting != null) {
			setting.setValueIfSameType(currentSetting);
			currentSetting.setSettingOwner(null);
		}
		
		settings.put(setting.getName(), setting);
		setting.setSettingOwner(this);
	}
	
	public Collection<GSSetting<?>> getSettings() {
		return Collections.unmodifiableCollection(settings.values());
	}

	void settingChanged(GSSetting<?> setting) {
		owner.settingChanged(category, setting);
	}

	public GSSettingCategory getCategory() {
		return category;
	}

	public void readSettings(PacketByteBuf buffer) throws DecoderException {
		int remaining = buffer.readInt();
		while (remaining-- > 0) {
			String name = buffer.readString();
			String type = buffer.readString();
			int sizeInBytes = buffer.readInt();
			
			int settingEnd = buffer.readerIndex() + sizeInBytes;
			
			GSSetting<?> setting;
			
			GSISettingDecoder<?> decoder = owner.getSettingDecoder(type);
			if (decoder == null) {
				buffer.skipBytes(sizeInBytes);
				setting = new GSUnknownSetting(name);
			} else {
				setting = decoder.decodeSetting(name, buffer);

				int off = settingEnd - buffer.readerIndex();
				if (off > 0) {
					buffer.skipBytes(off);
				} else {
					buffer.readerIndex(settingEnd);
				}
			}
			
			GSSetting<?> currentSetting = getSetting(name);
			if (currentSetting != null) {
				currentSetting.setValueIfSameType(setting);
			} else {
				setting.setSettingOwner(this);
				settings.put(setting.getName(), setting);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void writeSettings(PacketByteBuf buffer) throws DecoderException {
		PacketByteBuf settingBuffer = new PacketByteBuf(Unpooled.buffer());
		
		buffer.writeInt(settings.size());
		for (GSSetting<?> setting : settings.values()) {
			buffer.writeString(setting.getName());
			
			@SuppressWarnings("rawtypes")
			GSISettingDecoder decoder = owner.getSettingDecoder(setting.getClass());
			if (decoder == null) {
				buffer.writeString(GSSettingManager.UNKNOWN_SETTING_TYPE);
				buffer.writeInt(0);
			} else {
				decoder.encodeSetting(settingBuffer, setting);

				buffer.writeString(decoder.getTypeString());
				buffer.writeInt(settingBuffer.writerIndex() - settingBuffer.readerIndex());
				buffer.writeBytes(settingBuffer);
				settingBuffer.clear();
			}
		}
		
		settingBuffer.release(settingBuffer.refCnt());
	}
}
