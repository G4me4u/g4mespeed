package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.GSISettingDecoder;
import com.g4mesoft.setting.types.GSBooleanSetting;

import net.minecraft.network.PacketByteBuf;

public class GSBooleanSettingDecoder implements GSISettingDecoder<GSBooleanSetting> {

	private static final String BOOLEAN_TYPE_STRING = "BOOL";
	
	@Override
	public GSBooleanSetting decodeSetting(String name, PacketByteBuf buffer) {
		boolean value = buffer.readBoolean();
		boolean defaultValue = buffer.readBoolean();
		boolean visibleInGui = buffer.readBoolean();
		
		GSBooleanSetting setting = new GSBooleanSetting(name, defaultValue, visibleInGui);
		setting.setValue(value);
		
		if (buffer.isReadable(1)) {
			// Only read when available to ensure backwards compatability
			setting.setEnabledInGui(buffer.readBoolean());
		}
		
		return setting;
	}

	@Override
	public void encodeSetting(PacketByteBuf buffer, GSBooleanSetting setting) {
		buffer.writeBoolean(setting.getValue());
		buffer.writeBoolean(setting.getDefaultValue());
		buffer.writeBoolean(setting.isVisibleInGui());
		buffer.writeBoolean(setting.isEnabledInGui());
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
