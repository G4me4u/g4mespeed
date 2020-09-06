package com.g4mesoft.packet;

import java.io.IOException;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.GSController;
import com.g4mesoft.registry.GSElementRegistry;

import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.thread.ThreadExecutor;

public class GSPacketManager {

	private static final Identifier GS_IDENTIFIER = new Identifier("mod/g4mespeed");
	
	private final GSPacketRegistryList registryList;
	
	public GSPacketManager() {
		registryList = new GSPacketRegistryList();
		
		G4mespeedMod.getExtensions().forEach(this::registerPackets);
		G4mespeedMod.addExtensionListener(this::registerPackets);
	}
	
	public GSExtensionUID getPacketExtensionUniqueId(GSIPacket packet) {
		return registryList.getPacketExtensionUID(packet.getClass());
	}

	public Packet<?> encodePacket(GSIPacket packet, GSController controller) {
		long packetId = registryList.getPacketId(packet.getClass());
		if (packetId == -1)
			return null;
		
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		
		buffer.writeLong(packetId);
		
		try {
			packet.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return controller.encodeCustomPayload(GS_IDENTIFIER, buffer);
	}
	
	public <T extends PacketListener> GSIPacket decodePacket(GSICustomPayloadHolder<T> customPayload,
	                                                         GSExtensionInfoList extensionInfoList, 
	                                                         T packetListener, ThreadExecutor<?> executor) {
		
		if (!GS_IDENTIFIER.equals(customPayload.getChannelGS()))
			return null;
		
		PacketByteBuf buffer = customPayload.getDataGS();

		try {
			GSIPacket packet = registryList.createNewPacket(buffer.readLong());

			if (packet == null)
				return null;
			
			if (packet.shouldForceMainThread())
			      NetworkThreadUtils.forceMainThread(customPayload, packetListener, executor);
				
			GSExtensionUID extensionUid = getPacketExtensionUniqueId(packet);
			GSExtensionInfo extensionInfo = extensionInfoList.getInfo(extensionUid);

			try {
				packet.read(buffer, extensionInfo);
			} catch (IOException e) {
				return null;
			}
			
			return packet;
		} finally {
			buffer.release();
		}
	}
	
	private void registerPackets(GSIExtension extension) {
		GSElementRegistry<GSIPacket> registry = new GSElementRegistry<>();
		extension.registerPackets(registry);
		registryList.addPacketRegistry(extension.getInfo().getUniqueId(), registry);
	}
}
