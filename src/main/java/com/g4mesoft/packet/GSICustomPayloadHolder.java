package com.g4mesoft.packet;

import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public interface GSICustomPayloadHolder {

	public Identifier getChannelGS();

	public PacketByteBuf getDataGS();
	
}
