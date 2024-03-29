package com.g4mesoft.module.translation;

import java.io.IOException;

import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSTranslationCachePacket implements GSIPacket {

	private GSExtensionUID extensionUid;
	private GSTranslationCache cache;
	
	public GSTranslationCachePacket() {
	}

	public GSTranslationCachePacket(GSExtensionUID uid, GSTranslationCache cache) {
		this.extensionUid = uid;
		this.cache = cache;
	}
	
	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		extensionUid = GSExtensionUID.read(buf);
		cache = GSTranslationCache.read(buf);
	}

	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		GSExtensionUID.write(buf, extensionUid);
		GSTranslationCache.write(buf, cache);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller) {
		controller.getTranslationModule().addTranslationCache(extensionUid, cache);
	}
	
	@Override
	public boolean shouldForceMainThread() {
		return true;
	}
}
