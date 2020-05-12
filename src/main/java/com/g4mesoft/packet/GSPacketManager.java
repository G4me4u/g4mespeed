package com.g4mesoft.packet;

import java.io.IOException;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.registry.GSElementRegistry;

import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.thread.ThreadExecutor;

public class GSPacketManager {

	private static final Identifier GS_IDENTIFIER = new Identifier("mod/g4mespeed");
	
	private static final int MIN_CORE_FALLBACK_PACKET_ID = 0;
	private static final int MAX_CORE_FALLBACK_PACKET_ID = 8;
	
	private static final short INVALID_FALLBACK_PACKET_ID = (short)0xFFFF /* -1 */;
	
	private final GSPacketRegistryList registryList;
	
	public GSPacketManager() {
		registryList = new GSPacketRegistryList();
		
		G4mespeedMod.getExtensions().forEach(this::registerPackets);
		G4mespeedMod.addExtensionListener(this::registerPackets);
	}

	public Packet<?> encodePacket(GSIPacket packet, GSController controller) {
		long fullPacketId = registryList.getFullPacketId(packet.getClass());
		if (fullPacketId == -1)
			return null;
		
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

		if (isFallbackPacket(fullPacketId)) {
			buffer.writeShort(getFallbackId(fullPacketId));
		} else {
			buffer.writeShort(INVALID_FALLBACK_PACKET_ID);
			buffer.writeLong(fullPacketId);
		}
		
		try {
			packet.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return controller.encodeCustomPayload(GS_IDENTIFIER, buffer);
	}
	
	public <T extends PacketListener>GSIPacket decodePacket(GSICustomPayloadHolder<T> customPayload, GSVersion senderVersion, T packetListener, ThreadExecutor<?> executor) {
		if (!GS_IDENTIFIER.equals(customPayload.getChannelGS()))
			return null;
		
		PacketByteBuf buffer = customPayload.getDataGS();

		try {
			GSIPacket packet;

			short fallbackPacketId = buffer.readShort();
			if (fallbackPacketId != INVALID_FALLBACK_PACKET_ID) {
				packet = getFallbackRegistry().createNewElement((int)fallbackPacketId);
			} else {
				long fullPacketId = buffer.readLong();
				packet = registryList.createNewPacket(fullPacketId);
			}
			
			if (packet == null)
				return null;
			
			if (packet.shouldForceMainThread())
			      NetworkThreadUtils.forceMainThread(customPayload, packetListener, executor);
			
			try {
				packet.read(buffer, senderVersion);
			} catch (IOException e) {
				return null;
			}
			
			return packet;
		} finally {
			buffer.release();
		}
	}
	
	private boolean isFallbackPacket(long fullPacketId) {
		if (!G4mespeedMod.CORE_EXTENSION_UID.equals(registryList.getPacketExtensionUID(fullPacketId)))
			return false;

		// Backwards compatibility with older versions rely
		// on the use of short (2 byte) packet IDs.
		int packetId = registryList.getBasicPacketId(fullPacketId);
		if (packetId < MIN_CORE_FALLBACK_PACKET_ID)
			return false;
		if (packetId > MAX_CORE_FALLBACK_PACKET_ID)
			return false;
		return true;
	}

	private short getFallbackId(long fullPacketId) {
		return (short)fullPacketId;
	}
	
	private GSElementRegistry<GSIPacket> getFallbackRegistry() {
		return registryList.getPacketRegistry(G4mespeedMod.CORE_EXTENSION_UID);
	}

	private void registerPackets(GSIExtension extension) {
		GSElementRegistry<GSIPacket> registry = new GSElementRegistry<GSIPacket>();
		extension.registerPackets(registry);
		registryList.addPacketRegistry(extension.getUniqueId(), registry);
	}
}
