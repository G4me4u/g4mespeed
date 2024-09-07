package com.g4mesoft.packet;

import java.io.IOException;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.core.GSController;
import com.g4mesoft.registry.GSSupplierRegistry;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

public class GSPacketManager {

	private static final Identifier GS_IDENTIFIER = new Identifier("mod/g4mespeed");
	
	private final GSPacketRegistryList registryList;
	private boolean initialized;
	
	public GSPacketManager() {
		registryList = new GSPacketRegistryList();
		initialized = false;
	}
	
	public void init() {
		if (initialized)
			throw new IllegalStateException("Already initialized");
		
		G4mespeedMod.getExtensions().forEach(this::registerPackets);
		G4mespeedMod.addExtensionListener(this::registerPackets);
	
		initialized = true;
	}
	
	public GSExtensionUID getPacketExtensionUniqueId(GSIPacket packet) {
		return registryList.getPacketExtensionUID(packet.getClass());
	}

	public Packet<?> encodePacket(GSIPacket packet, GSController controller) {
		long packetId = registryList.getPacketId(packet.getClass());
		if (packetId == -1)
			return null;
		
		ByteBuf buf = Unpooled.buffer();
		buf.writeLong(packetId);
		
		try {
			packet.write(GSEncodeBuffer.wrap(buf));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return controller.createCustomPayload(GS_IDENTIFIER, new PacketByteBuf(buf));
	}
	
	public <T extends PacketListener> GSIPacket decodePacket(GSICustomPayloadPacket<T> customPayload,
	                                                         GSExtensionInfoList extensionInfoList, 
	                                                         T packetListener, ThreadExecutor<?> executor) {
		
		if (!GS_IDENTIFIER.equals(customPayload.getChannel0()))
			return null;
		
		PacketByteBuf buffer = customPayload.getData0();

		GSIPacket packet = registryList.createNewPacket(buffer.readLong());
		if (packet == null) {
			buffer.release();
			return null;
		}
		if (packet.shouldForceMainThread())
		      NetworkThreadUtils.forceMainThread(customPayload, packetListener, executor);
			
		GSExtensionUID extensionUid = getPacketExtensionUniqueId(packet);
		GSExtensionInfo extensionInfo = extensionInfoList.getInfo(extensionUid);

		try {
			packet.read(GSDecodeBuffer.wrap(buffer), extensionInfo);
		} catch (IOException e) {
			return null;
		} finally {
			buffer.release();
		}
		
		return packet;
	}
	
	private void registerPackets(GSIExtension extension) {
		GSSupplierRegistry<Integer, GSIPacket> registry = new GSSupplierRegistry<>();
		extension.registerPackets(registry);
		registryList.addPacketRegistry(extension.getInfo().getUniqueId(), registry);
	}
}
