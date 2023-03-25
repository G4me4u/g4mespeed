package com.g4mesoft.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

public interface GSICustomPayloadPacket<T extends PacketListener> extends Packet<T> {

	public Identifier getChannel0();

	public PacketByteBuf getData0();
	
}
