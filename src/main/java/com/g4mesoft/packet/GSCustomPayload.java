package com.g4mesoft.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class GSCustomPayload implements CustomPayload {

	public static final Identifier GS_IDENTIFIER = new Identifier("mod/g4mespeed");
	
	private final ByteBuf buffer;
	
	public GSCustomPayload(PacketByteBuf buf) {
        buffer = buf.readBytes(buf.readableBytes());
	}
	
	private GSCustomPayload(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public ByteBuf getBuffer() {
		return buffer.copy();
	}
	
	@Override
	public void write(PacketByteBuf buf) {
		// Note: slice will maintain its own reader and writer index,
		//       but does not modify the existing indices.
		buf.writeBytes(buffer.slice());
	}

	@Override
	public Identifier id() {
		return GS_IDENTIFIER;
	}
	
	public static GSCustomPayload create(ByteBuf buffer) {
		return new GSCustomPayload((ByteBuf)buffer);
	}
}
