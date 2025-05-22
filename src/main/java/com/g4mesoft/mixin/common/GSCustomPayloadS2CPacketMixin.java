package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.packet.GSICustomPayloadPacket;

import net.minecraft.client.network.handler.ClientPlayPacketHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

@Mixin(CustomPayloadS2CPacket.class)
public abstract class GSCustomPayloadS2CPacketMixin implements GSICustomPayloadPacket<ClientPlayPacketHandler> {

	@Shadow private String channel;
	@Shadow private PacketByteBuf data;
	
	@Override
	public String getChannel0() {
		return channel;
	}

	@Override
	public PacketByteBuf getData0() {
		return new PacketByteBuf(data.copy());
	}
}
