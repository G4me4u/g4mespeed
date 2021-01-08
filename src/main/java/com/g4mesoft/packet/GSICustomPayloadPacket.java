package com.g4mesoft.packet;

import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public interface GSICustomPayloadPacket<T extends PacketListener> extends Packet<T> {

	public Identifier getChannel0();

	public PacketByteBuf getData0();
	
}
