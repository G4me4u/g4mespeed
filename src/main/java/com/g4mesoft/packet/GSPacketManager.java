package com.g4mesoft.packet;

import java.io.IOException;

import com.g4mesoft.GSController;
import com.g4mesoft.tickspeed.GSTpsChangePacket;
import com.g4mesoft.tickspeed.GSTpsResetPacket;

import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class GSPacketManager {

	private static final Identifier GS_IDENTIFIER = new Identifier("mod/g4mespeed");
	
	private final GSPacketRegistry registry;
	
	@SuppressWarnings("unchecked")
	public GSPacketManager() {
		registry = new GSPacketRegistry(new Class[] {
			GSTpsResetPacket.class,
			GSTpsChangePacket.class
		});
	}

	public Packet<?> encodePacket(GSIPacket packet, GSController controller) {
		int packetIndex = registry.getPacketIndex(packet);
		if (packetIndex == -1)
			return null;
		
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

		buffer.writeShort((short)packetIndex);
		try {
			packet.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return controller.encodeCustomPayload(GS_IDENTIFIER, buffer);
	}

	public GSIPacket decodePacket(GSICustomPayloadHolder customPayload) {
		if (!GS_IDENTIFIER.equals(customPayload.getChannelGS()))
			return null;
		
		PacketByteBuf buffer = customPayload.getDataGS();

		try {
			int packetIndex = (int)buffer.readShort() & 0xFFFF;
			
			Class<? extends GSIPacket> packetClazz = registry.getPacketClass(packetIndex);
			if (packetClazz == null)
				return null;
			
			GSIPacket packet;
			try {
				packet = packetClazz.newInstance();
				packet.read(buffer);
			} catch(InstantiationException | IllegalAccessException | IOException e) {
				return null;
			}
			
			return packet;
		} finally {
			buffer.release();
		}
	}
}
