package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.GSISettingDecoder;
import com.g4mesoft.setting.types.GSFloatSetting;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

public class GSFloatSettingDecoder implements GSISettingDecoder<GSFloatSetting> {

	private static final String FLOAT_TYPE_STRING = "FLT";
	
	@Override
	public GSFloatSetting decodeSetting(String name, GSDecodeBuffer buf) {
		float value = buf.readFloat();
		float defaultValue = buf.readFloat();
		float minValue = buf.readFloat();
		float maxValue = buf.readFloat();
		float interval = buf.readFloat();
		boolean visibleInGui = buf.readBoolean();
		
		GSFloatSetting setting = new GSFloatSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui);
		setting.set(value);

		if (buf.isReadable(1)) {
			// Only read when available to ensure backwards compatability
			setting.setEnabledInGui(buf.readBoolean());
		}
		
		return setting;
	}

	@Override
	public void encodeSetting(GSEncodeBuffer buf, GSFloatSetting setting) {
		buf.writeFloat(setting.get());
		buf.writeFloat(setting.getDefault());
		buf.writeFloat(setting.getMin());
		buf.writeFloat(setting.getMax());
		buf.writeFloat(setting.getInterval());
		buf.writeBoolean(setting.isVisibleInGui());
		buf.writeBoolean(setting.isEnabledInGui());
	}

	@Override
	public String getTypeString() {
		return FLOAT_TYPE_STRING;
	}

	@Override
	public Class<GSFloatSetting> getSettingClass() {
		return GSFloatSetting.class;
	}
}
