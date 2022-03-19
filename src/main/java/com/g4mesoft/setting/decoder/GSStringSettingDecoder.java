package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.types.GSStringSetting;
import com.g4mesoft.util.GSBufferUtil;

import net.minecraft.network.PacketByteBuf;

public class GSStringSettingDecoder implements GSISettingDecoder<GSStringSetting> {

	private static final String STRING_TYPE_STRING = "STR";
	
	@Override
	public GSStringSetting decodeSetting(String name, PacketByteBuf buffer) {
		String value = buffer.readString(GSBufferUtil.MAX_STRING_LENGTH);
		String defaultValue = buffer.readString(GSBufferUtil.MAX_STRING_LENGTH);
		boolean visibleInGui = buffer.readBoolean();
		
		GSStringSetting setting = new GSStringSetting(name, defaultValue, visibleInGui);
		setting.setValue(value);

		if (buffer.isReadable(1)) {
			// Only read when available to ensure backwards compatability
			setting.setEnabledInGui(buffer.readBoolean());
		}
		
		return setting;
	}

	@Override
	public void encodeSetting(PacketByteBuf buffer, GSStringSetting setting) {
		buffer.writeString(setting.getValue());
		buffer.writeString(setting.getDefaultValue());
		buffer.writeBoolean(setting.isVisibleInGui());
		buffer.writeBoolean(setting.isEnabledInGui());
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
