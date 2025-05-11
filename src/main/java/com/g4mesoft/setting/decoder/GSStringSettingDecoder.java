package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.GSISettingDecoder;
import com.g4mesoft.setting.types.GSStringSetting;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

public class GSStringSettingDecoder implements GSISettingDecoder<GSStringSetting> {

	private static final String STRING_TYPE_STRING = "STR";
	
	@Override
	public GSStringSetting decodeSetting(String name, GSDecodeBuffer buf) {
		String value = buf.readString();
		String defaultValue = buf.readString();
		boolean visibleInGui = buf.readBoolean();
		
		GSStringSetting setting = new GSStringSetting(name, defaultValue, visibleInGui);
		setting.set(value);

		if (buf.isReadable(1)) {
			// Only read when available to ensure backwards compatability
			setting.setEnabledInGui(buf.readBoolean());
		}
		
		return setting;
	}

	@Override
	public void encodeSetting(GSEncodeBuffer buf, GSStringSetting setting) {
		buf.writeString(setting.get());
		buf.writeString(setting.getDefault());
		buf.writeBoolean(setting.isVisibleInGui());
		buf.writeBoolean(setting.isEnabledInGui());
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
