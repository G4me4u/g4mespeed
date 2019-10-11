package com.g4mesoft.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.setting.decoder.GSISettingDecoder;
import com.g4mesoft.setting.types.GSUnknownSetting;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.util.PacketByteBuf;

public final class GSSettingMap {

	private final GSSettingCategory category;
	private final Map<String, GSSetting<?>> settings;
	
	private GSSettingManager owner;
	
	public GSSettingMap(GSSettingCategory category) {
		this.category = category;
		
		settings = new HashMap<String, GSSetting<?>>();
	}
	
	public GSSetting<?> getSetting(String name) {
		return settings.get(name);
	}
	
	private void addSetting(GSSetting<?> setting, boolean replaceIfExists, boolean loaded) {
		GSSetting<?> currentSetting = getSetting(setting.getName());
		
		if (replaceIfExists) {
			if (currentSetting != null) {
				setting.setValueIfSameType(currentSetting);
				currentSetting.setSettingOwner(null);
				
				if (owner != null)
					owner.settingRemoved(category, currentSetting);
			}
			
			settings.put(setting.getName(), setting);
			setting.setSettingOwner(this);
			
			if (owner != null)
				owner.settingAdded(category, setting, loaded);
		} else {
			if (currentSetting != null) {
				currentSetting.setValueIfSameType(setting);
			} else {
				setting.setSettingOwner(this);
				settings.put(setting.getName(), setting);

				if (owner != null)
					owner.settingAdded(category, setting, loaded);
			}
		}
	}
	
	public void registerSetting(GSSetting<?> setting) {
		addSetting(setting, true, false);
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
	
	public void transferSettings(GSSettingMap other) {
		List<GSSetting<?>> settings = new ArrayList<GSSetting<?>>(other.settings.values());
		other.clearSettings();
		
		for (GSSetting<?> setting : settings)
			addSetting(setting, false, true);
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
			
			addSetting(setting, false, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void writeSettings(PacketByteBuf buffer) throws DecoderException {
		PacketByteBuf settingBuffer = new PacketByteBuf(Unpooled.buffer());
		
		buffer.writeInt(settings.size());
		for (GSSetting<?> setting : settings.values()) {
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
		
		settingBuffer.release(settingBuffer.refCnt());
	}
	
	public void setSettingOwner(GSSettingManager owner) {
		this.owner = owner;
	}

	public GSSettingManager getSettingOwner() {
		return owner;
	}
}
