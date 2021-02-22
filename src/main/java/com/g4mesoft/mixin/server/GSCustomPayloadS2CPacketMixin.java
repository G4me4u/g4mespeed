package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.packet.GSICustomPayloadPacket;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

@Mixin(CustomPayloadS2CPacket.class)
public abstract class GSCustomPayloadS2CPacketMixin implements GSICustomPayloadPacket<ClientPlayPacketListener> {

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
