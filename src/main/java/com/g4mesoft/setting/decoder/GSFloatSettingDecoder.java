package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.GSISettingDecoder;
import com.g4mesoft.setting.types.GSFloatSetting;

import net.minecraft.network.PacketByteBuf;

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
		
		GSFloatSetting setting = new GSFloatSetting(name, defaultValue, minValue, maxValue, interval, visibleInGui);
		setting.set(value);

		if (buffer.isReadable(1)) {
			// Only read when available to ensure backwards compatability
			setting.setEnabledInGui(buffer.readBoolean());
		}
		
		return setting;
	}

	@Override
	public void encodeSetting(PacketByteBuf buffer, GSFloatSetting setting) {
		buffer.writeFloat(setting.get());
		buffer.writeFloat(setting.getDefault());
		buffer.writeFloat(setting.getMin());
		buffer.writeFloat(setting.getMax());
		buffer.writeFloat(setting.getInterval());
		buffer.writeBoolean(setting.isVisibleInGui());
		buffer.writeBoolean(setting.isEnabledInGui());
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
