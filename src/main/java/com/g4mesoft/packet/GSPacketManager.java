package com.g4mesoft.packet;

import java.io.IOException;

import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSVersionPacket;
import com.g4mesoft.module.tps.GSServerSyncPacket;
import com.g4mesoft.module.tps.GSTpsChangePacket;
import com.g4mesoft.module.tps.GSTpsHotkeyPacket;
import com.g4mesoft.module.translation.GSTranslationCachePacket;
import com.g4mesoft.module.translation.GSTranslationVersionPacket;
import com.g4mesoft.setting.GSServerSettingMapPacket;
import com.g4mesoft.setting.GSSettingChangePacket;

import io.netty.buffer.Unpooled;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.ThreadExecutor;

public class GSPacketManager {

	private static final Identifier GS_IDENTIFIER = new Identifier("mod/g4mespeed");
	
	private final GSPacketRegistry registry;
	
	@SuppressWarnings("unchecked")
	public GSPacketManager() {
		registry = new GSPacketRegistry(new Class[] {
			GSVersionPacket.class,
			
			GSTpsHotkeyPacket.class,
			GSTpsChangePacket.class,
			GSServerSyncPacket.class,
			
			GSTranslationVersionPacket.class,
			GSTranslationCachePacket.class,
			
			GSServerSettingMapPacket.class,
			GSSettingChangePacket.class
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

	public <T extends PacketListener>GSIPacket decodePacket(GSICustomPayloadHolder<T> customPayload, T packetListener, ThreadExecutor<?> executor) {
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
			} catch(InstantiationException | IllegalAccessException e) {
				return null;
			}
			
			if (packet.shouldForceMainThread())
			      NetworkThreadUtils.forceMainThread(customPayload, packetListener, executor);
				
			try {
				packet.read(buffer);
			} catch (IOException e) {
				return null;
			}
			
			return packet;
		} finally {
			buffer.release();
		}
	}
}
