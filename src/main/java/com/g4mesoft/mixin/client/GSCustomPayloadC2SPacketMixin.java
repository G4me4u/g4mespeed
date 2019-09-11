package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.packet.GSICustomPayloadHolder;

import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

@Mixin(CustomPayloadC2SPacket.class)
public class GSCustomPayloadC2SPacketMixin implements GSICustomPayloadHolder {

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
