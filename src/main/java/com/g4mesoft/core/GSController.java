package com.g4mesoft.core;

import com.g4mesoft.tps.GSITpsDependant;

import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public abstract class GSController implements GSITpsDependant {

	public abstract Packet<?> encodeCustomPayload(Identifier identifier, PacketByteBuf buffer);

	public abstract boolean isClient();

	public abstract int getVersion();
	
}
