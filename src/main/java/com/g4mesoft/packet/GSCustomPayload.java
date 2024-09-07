package com.g4mesoft.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class GSCustomPayload implements CustomPayload {

	public static final CustomPayload.Id<GSCustomPayload> ID = CustomPayload.id("mod/g4mespeed");
	public static final PacketCodec<PacketByteBuf, GSCustomPayload> CODEC = CustomPayload.codecOf(GSCustomPayload::write, GSCustomPayload::new);
	
	private final ByteBuf buffer;
	
	public GSCustomPayload(PacketByteBuf buf) {
        buffer = buf.readBytes(buf.readableBytes());
	}
	
	private GSCustomPayload(ByteBuf buffer, boolean ignore) {
		this.buffer = buffer;
	}

	public ByteBuf getBuffer() {
		return buffer.copy();
	}
	
	public void write(PacketByteBuf buf) {
		// Note: slice will maintain its own reader and writer index,
		//       but does not modify the existing indices.
		buf.writeBytes(buffer.slice());
	}

	public static GSCustomPayload create(ByteBuf buffer) {
		return new GSCustomPayload(buffer, false);
	}

	@Override
	public Id<GSCustomPayload> getId() {
		return ID;
	}
}
