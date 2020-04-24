package com.g4mesoft.module.translation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSBufferUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSTranslationCachePacket implements GSIPacket {

	private byte uid;
	private GSTranslationCache cache;
	
	public GSTranslationCachePacket() {
	}

	public GSTranslationCachePacket(byte uid, GSTranslationCache cache) {
		this.uid = uid;
		this.cache = cache;
	}
	
	@Override
	public void read(PacketByteBuf buf, GSVersion senderVersion) throws IOException {
		read(buf);
		
		if (senderVersion.isGreaterThanOrEqualTo(GSTranslationModule.TRANSLATION_EXTENSION_VERSION)) {
			uid = buf.readByte();
		} else {
			uid = G4mespeedMod.CORE_EXTENSION_UID;
		}
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		int cacheVersion = buf.readInt();
		
		int n = buf.readInt();
		
		Map<String, String> translations = new HashMap<String, String>(n);
		while (n-- > 0) {
			String key = buf.readString(GSBufferUtil.MAX_STRING_LENGTH);
			String value = buf.readString(GSBufferUtil.MAX_STRING_LENGTH);
			translations.put(key, value);
		}
		
		cache = new GSTranslationCache(cacheVersion, translations);
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeInt(cache.getCacheVersion());
		
		Map<String, String> translations = cache.getTranslationMap();
		buf.writeInt(translations.size());
		for (Map.Entry<String, String> entry : translations.entrySet()) {
			buf.writeString(entry.getKey());
			buf.writeString(entry.getValue());
		}
		
		buf.writeByte(uid);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.getTranslationModule().addTranslationCache(uid, cache);
	}
	
	@Override
	public boolean shouldForceMainThread() {
		return true;
	}
}
