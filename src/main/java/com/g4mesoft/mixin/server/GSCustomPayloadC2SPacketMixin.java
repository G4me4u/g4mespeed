package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.packet.GSICustomPayloadHolder;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

@Mixin(CustomPayloadC2SPacket.class)
public abstract class GSCustomPayloadC2SPacketMixin implements GSICustomPayloadHolder<ServerPlayPacketListener> {

	@Shadow private Identifier channel;
	@Shadow private PacketByteBuf data;
	
	@Override
	public Identifier getChannelGS() {
		return channel;
	}

	@Override
	public PacketByteBuf getDataGS() {
		return new PacketByteBuf(data.copy());
	}
}
