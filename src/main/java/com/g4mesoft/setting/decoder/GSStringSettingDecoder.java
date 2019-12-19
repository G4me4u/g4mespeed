package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.types.GSStringSetting;

import net.minecraft.util.PacketByteBuf;

public class GSStringSettingDecoder implements GSISettingDecoder<GSStringSetting> {

	private static final String STRING_TYPE_STRING = "STR";
	
	@Override
	public GSStringSetting decodeSetting(String name, PacketByteBuf buffer) {
		String value = buffer.readString(32767);
		String defaultValue = buffer.readString(32767);
		boolean visibleInGui = buffer.readBoolean();
		
		return new GSStringSetting(name, defaultValue, visibleInGui).setValue(value);
	}

	@Override
	public void encodeSetting(PacketByteBuf buffer, GSStringSetting setting) {
		buffer.writeString(setting.getValue());
		buffer.writeString(setting.getDefaultValue());
		buffer.writeBoolean(setting.isVisibleInGUI());
	}

	@Override
	public String getTypeString() {
		return STRING_TYPE_STRING;
	}

	@Override
	public Class<GSStringSetting> getSettingClass() {
		return GSStringSetting.class;
	}
}
