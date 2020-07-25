package com.g4mesoft.module.translation;

import java.io.IOException;

import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

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
	public void read(PacketByteBuf buf) throws IOException {
		extensionUid = GSExtensionUID.read(buf);
		cache = GSTranslationCache.read(buf);
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		GSExtensionUID.write(buf, extensionUid);
		GSTranslationCache.write(buf, cache);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.getTranslationModule().addTranslationCache(extensionUid, cache);
	}
	
	@Override
	public boolean shouldForceMainThread() {
		return true;
	}
}
