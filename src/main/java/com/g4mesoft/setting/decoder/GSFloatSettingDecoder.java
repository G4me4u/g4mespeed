package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.types.GSFloatSetting;

import net.minecraft.util.PacketByteBuf;

public class GSFloatSettingDecoder implements GSISettingDecoder<GSFloatSetting> {

	private static final String FLOAT_TYPE_STRING = "FLT";
	
	@Override
	public GSFloatSetting decodeSetting(String name, PacketByteBuf buffer) {
		float value = buffer.readFloat();
		float defaultValue = buffer.readFloat();
		float minValue = buffer.readFloat();
		float maxValue = buffer.readFloat();
		float interval = buffer.readFloat();
		boolean visibleInGui = buffer.readBoolean();
		
		return new GSFloatSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui).setValue(value);
	}

	@Override
	public void encodeSetting(PacketByteBuf buffer, GSFloatSetting setting) {
		buffer.writeFloat(setting.getValue());
		buffer.writeFloat(setting.getDefaultValue());
		buffer.writeFloat(setting.getMinValue());
		buffer.writeFloat(setting.getMaxValue());
		buffer.writeFloat(setting.getInterval());
		buffer.writeBoolean(setting.isVisibleInGUI());
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
