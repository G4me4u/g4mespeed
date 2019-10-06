package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.types.GSBooleanSetting;

import net.minecraft.util.PacketByteBuf;

public class GSBooleanSettingDecoder implements GSISettingDecoder<GSBooleanSetting> {

	private static final String BOOLEAN_TYPE_STRING = "BOOL";
	
	@Override
	public GSBooleanSetting decodeSetting(String name, PacketByteBuf buffer) {
		boolean value = buffer.readBoolean();
		boolean defaultValue = buffer.readBoolean();
		
		GSBooleanSetting setting = new GSBooleanSetting(name, defaultValue);
		setting.setValue(value);
		return setting;
	}

	@Override
	public void encodeSetting(PacketByteBuf buffer, GSBooleanSetting setting) {
		buffer.writeBoolean(setting.getValue());
		buffer.writeBoolean(setting.getDefaultValue());
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
