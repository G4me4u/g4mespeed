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

public class GSTranslationCachePacket implements GSIPacket {

	private GSTranslationCache cache;
	
	public GSTranslationCachePacket() {
	}

	public GSTranslationCachePacket(GSTranslationCache cache) {
		this.cache = cache;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		int cacheVersion = buf.readInt();
		
		int n = buf.readInt();
		
		Map<String, String> translations = new HashMap<String, String>(n);
		while (n-- > 0) {
			String key = buf.readString(32767);
			String value = buf.readString(32767);
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
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		System.out.println("VERSION===!=!=!=!=!=!===== ->" + cache.getCacheVersion());
		
		controller.getTranslationModule().addTranslationCache(cache);
	}
	
	@Override
	public boolean shouldForceMainThread() {
		return true;
	}
}
