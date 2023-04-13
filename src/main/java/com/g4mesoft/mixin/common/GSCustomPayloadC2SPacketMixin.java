package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.packet.GSICustomPayloadPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

@Mixin(CustomPayloadC2SPacket.class)
public abstract class GSCustomPayloadC2SPacketMixin implements GSICustomPayloadPacket<ServerPlayPacketListener> {

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
