package com.g4mesoft.setting;

import net.minecraft.network.PacketByteBuf;

public interface GSISettingDecoder<T extends GSSetting<?>> {

	public T decodeSetting(String name, PacketByteBuf buffer);

	public void encodeSetting(PacketByteBuf buffer, T setting);

	public String getTypeString();
	
	public Class<T> getSettingClass();
	
}
