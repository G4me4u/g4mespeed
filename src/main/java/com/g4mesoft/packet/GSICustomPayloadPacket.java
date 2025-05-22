package com.g4mesoft.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.handler.PacketHandler;
import net.minecraft.network.packet.Packet;

public interface GSICustomPayloadPacket<T extends PacketHandler> extends Packet<T> {

	public String getChannel0();

	public PacketByteBuf getData0();
	
}
