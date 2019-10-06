package com.g4mesoft.setting.decoder;

import com.g4mesoft.setting.GSSetting;

import net.minecraft.util.PacketByteBuf;

@SuppressWarnings("rawtypes")
public interface GSISettingDecoder<T extends GSSetting> {

	public T decodeSetting(String name, PacketByteBuf buffer);

	public void encodeSetting(PacketByteBuf buffer, T setting);

	public String getTypeString();
	
	public Class<T> getSettingClass();
	
}
