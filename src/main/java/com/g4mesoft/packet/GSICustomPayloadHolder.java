package com.g4mesoft.packet;

import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

public interface GSICustomPayloadHolder<T extends PacketListener> extends Packet<T> {

	public Identifier getChannelGS();

	public PacketByteBuf getDataGS();
	
}
