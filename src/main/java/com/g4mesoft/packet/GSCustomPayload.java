package com.g4mesoft.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class GSCustomPayload implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<GSCustomPayload> ID = CustomPacketPayload.createType("mod/g4mespeed");
	public static final StreamCodec<FriendlyByteBuf, GSCustomPayload> CODEC = CustomPacketPayload.codec(GSCustomPayload::write, GSCustomPayload::new);
	
	private final ByteBuf buffer;
	
	public GSCustomPayload(FriendlyByteBuf buf) {
        buffer = buf.readBytes(buf.readableBytes());
	}
	
	private GSCustomPayload(ByteBuf buffer, boolean ignore) {
		this.buffer = buffer;
	}

	public ByteBuf getBuffer() {
		return buffer.copy();
	}
	
	public void write(FriendlyByteBuf buf) {
		// Note: slice will maintain its own reader and writer index,
		//       but does not modify the existing indices.
		buf.writeBytes(buffer.slice());
	}

	public static GSCustomPayload create(ByteBuf buffer) {
		return new GSCustomPayload(buffer, false);
	}

	@Override
	public Type<GSCustomPayload> type() {
		return ID;
	}
}
