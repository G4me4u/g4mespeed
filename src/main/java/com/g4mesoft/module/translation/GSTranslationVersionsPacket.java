package com.g4mesoft.module.translation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSTranslationVersionsPacket implements GSIPacket {

	private Map<Byte, Integer> uidToVersion;
	
	public GSTranslationVersionsPacket() {
	}

	public GSTranslationVersionsPacket(Map<Byte, GSTranslationCacheList> cacheLists) {
		uidToVersion = new HashMap<Byte, Integer>();
		
		for (Map.Entry<Byte, GSTranslationCacheList> entry : cacheLists.entrySet())
			uidToVersion.put(entry.getKey(), entry.getValue().getVersion());
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		int n = buf.readInt();

		uidToVersion = new HashMap<Byte, Integer>();
		while (n-- != 0) {
			byte uid = buf.readByte();
			int version = buf.readInt();
			uidToVersion.put(uid, version);
		}
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeInt(uidToVersion.size());
		
		for (Map.Entry<Byte, Integer> entry : uidToVersion.entrySet()) {
			buf.writeByte(entry.getKey().byteValue());
			buf.writeInt(entry.getValue().intValue());
		}
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		controller.getTranslationModule().onTranslationVersionsReceived(player, uidToVersion);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
	}
}
