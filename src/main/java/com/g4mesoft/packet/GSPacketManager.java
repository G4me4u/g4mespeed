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
	
	private final GSPacketRegistryList registryList;
	
	public GSPacketManager() {
		registryList = new GSPacketRegistryList();
		
		G4mespeedMod.getExtensions().forEach(this::registerPackets);
		G4mespeedMod.addExtensionListener(this::registerPackets);
	}

	public Packet<?> encodePacket(GSIPacket packet, GSController controller) {
		int packetId = registryList.getPacketId(packet.getClass());
		if (packetId == -1)
			return null;
		
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

		buffer.writeShort((short)packetId);
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
			short packetId = buffer.readShort();
			GSIPacket packet = registryList.createNewPacket(packetId);
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

	private void registerPackets(GSIExtension extension) {
		GSElementRegistry<GSIPacket> registry = new GSElementRegistry<GSIPacket>();
		extension.registerPackets(registry);
		registryList.addPacketRegistry(extension.getUniqueId(), registry);
	}
}
