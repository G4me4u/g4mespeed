package com.g4mesoft.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.handler.PacketHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.resource.Identifier;

public interface GSICustomPayloadPacket<T extends PacketHandler> extends Packet<T> {

	public Identifier getChannel0();

	public PacketByteBuf getData0();
	
}
