package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.GSISettingDecoder;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

public class GSBooleanSettingDecoder implements GSISettingDecoder<GSBooleanSetting> {

	private static final String BOOLEAN_TYPE_STRING = "BOOL";
	
	@Override
	public GSBooleanSetting decodeSetting(String name, GSDecodeBuffer buf) {
		boolean value = buf.readBoolean();
		boolean defaultValue = buf.readBoolean();
		boolean visibleInGui = buf.readBoolean();
		
		GSBooleanSetting setting = new GSBooleanSetting(name, defaultValue, visibleInGui);
		setting.set(value);
		
		if (buf.isReadable(1)) {
			// Only read when available to ensure backwards compatability
			setting.setEnabledInGui(buf.readBoolean());
		}
		
		return setting;
	}

	@Override
	public void encodeSetting(GSEncodeBuffer buf, GSBooleanSetting setting) {
		buf.writeBoolean(setting.get());
		buf.writeBoolean(setting.getDefault());
		buf.writeBoolean(setting.isVisibleInGui());
		buf.writeBoolean(setting.isEnabledInGui());
	}

	@Override
	public String getTypeString() {
		return BOOLEAN_TYPE_STRING;
	}

	@Override
	public Class<GSBooleanSetting> getSettingClass() {
		return GSBooleanSetting.class;
	}
}
