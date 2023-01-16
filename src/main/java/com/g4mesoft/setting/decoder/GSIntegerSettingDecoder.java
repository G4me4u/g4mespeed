package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.GSISettingDecoder;
import com.g4mesoft.setting.types.GSIntegerSetting;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

public class GSIntegerSettingDecoder implements GSISettingDecoder<GSIntegerSetting> {

	private static final String INTEGER_TYPE_STRING = "INT";
	
	@Override
	public GSIntegerSetting decodeSetting(String name, GSDecodeBuffer buf) {
		int value = buf.readInt();
		int defaultValue = buf.readInt();
		int minValue = buf.readInt();
		int maxValue = buf.readInt();
		int interval = buf.readInt();
		boolean visibleInGui = buf.readBoolean();
		
		GSIntegerSetting setting = new GSIntegerSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui);
		setting.set(value);

		if (buf.isReadable(1)) {
			// Only read when available to ensure backwards compatability
			setting.setEnabledInGui(buf.readBoolean());
		}
		
		return setting;
	}

	@Override
	public void encodeSetting(GSEncodeBuffer buf, GSIntegerSetting setting) {
		buf.writeInt(setting.get());
		buf.writeInt(setting.getDefault());
		buf.writeInt(setting.getMin());
		buf.writeInt(setting.getMax());
		buf.writeInt(setting.getInterval());
		buf.writeBoolean(setting.isVisibleInGui());
		buf.writeBoolean(setting.isEnabledInGui());
	}

	@Override
	public String getTypeString() {
		return INTEGER_TYPE_STRING;
	}

	@Override
	public Class<GSIntegerSetting> getSettingClass() {
		return GSIntegerSetting.class;
	}
}
