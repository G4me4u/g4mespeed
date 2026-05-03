package com.g4mesoft.packet;

import java.io.IOException;
import java.util.function.Consumer;

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
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.thread.BlockableEventLoop;

public class GSPacketManager {

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
		
		return controller.createCustomPayload(buf);
	}
	
	public <T extends PacketListener> GSIPacket decodePacket(CustomPacketPayload payload, GSExtensionInfoList extensionInfoList) {
		if (!(payload instanceof GSCustomPayload))
			return null;
		
		ByteBuf buffer = ((GSCustomPayload)payload).getBuffer();
		
		GSIPacket packet = registryList.createNewPacket(buffer.readLong());
		if (packet == null) {
			buffer.release();
			return null;
		}
		
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
	
	public <T extends PacketListener> void handlePacket(GSIPacket packet, T packetListener, BlockableEventLoop<?> executor, Consumer<GSIPacket> handler) {
		if (packet.shouldForceMainThread() && !executor.isSameThread()) {
			// Schedule the handler on the main thread.
			executor.executeIfPossible(() -> {
				if (packetListener.isAcceptingMessages()) {
					try {
						handler.accept(packet);
					} catch (Exception e) {
						// Throw exception if we are out of memory
                        if (e instanceof ReportedException && ((ReportedException)e).getCause() instanceof OutOfMemoryError)
                            throw e;
						// Ignore exception and continue.
						G4mespeedMod.GS_LOGGER.error("Failed to handle packet {}, suppressing error", packet, e);
					}
				} else {
					G4mespeedMod.GS_LOGGER.debug("Ignoring packet due to disconnection: {}", packet);
				}
			});
		} else {
			// Otherwise, handle packet now.
			handler.accept(packet);
		}
	}
	
	private void registerPackets(GSIExtension extension) {
		GSSupplierRegistry<Integer, GSIPacket> registry = new GSSupplierRegistry<>();
		extension.registerPackets(registry);
		registryList.addPacketRegistry(extension.getInfo().getUniqueId(), registry);
	}
}
