package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.types.GSIntegerSetting;

import net.minecraft.util.PacketByteBuf;

public class GSIntegerSettingDecoder implements GSISettingDecoder<GSIntegerSetting> {

	private static final String INTEGER_TYPE_STRING = "INT";
	
	@Override
	public GSIntegerSetting decodeSetting(String name, PacketByteBuf buffer) {
		int value = buffer.readInt();
		int defaultValue = buffer.readInt();
		int minValue = buffer.readInt();
		int maxValue = buffer.readInt();
		int interval = buffer.readInt();
		boolean availableInGui = buffer.readBoolean();		
		
		GSIntegerSetting setting = new GSIntegerSetting(name, defaultValue, minValue, maxValue, interval, availableInGui);
		setting.setValue(value);
		return setting;
	}

	@Override
	public void encodeSetting(PacketByteBuf buffer, GSIntegerSetting setting) {
		buffer.writeInt(setting.getValue());
		buffer.writeInt(setting.getDefaultValue());
		buffer.writeInt(setting.getMinValue());
		buffer.writeInt(setting.getMaxValue());
		buffer.writeInt(setting.getInterval());
		buffer.writeBoolean(setting.isAvailableInGUI());
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
