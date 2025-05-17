package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.packet.GSICustomPayloadPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.resource.Identifier;
import net.minecraft.server.network.handler.ServerPlayPacketHandler;

@Mixin(CustomPayloadC2SPacket.class)
public abstract class GSCustomPayloadC2SPacketMixin implements GSICustomPayloadPacket<ServerPlayPacketHandler	> {

	@Shadow private Identifier channel;
	@Shadow private PacketByteBuf data;
	
	@Override
	public Identifier getChannel0() {
		return channel;
	}

	@Override
	public PacketByteBuf getData0() {
		return new PacketByteBuf(data.copy());
	}
}
