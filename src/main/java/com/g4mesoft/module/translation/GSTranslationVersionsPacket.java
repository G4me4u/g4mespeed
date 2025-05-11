package com.g4mesoft.module.translation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSTranslationVersionsPacket implements GSIPacket {

	private Map<GSExtensionUID, Integer> uidToVersion;
	
	public GSTranslationVersionsPacket() {
	}

	public GSTranslationVersionsPacket(Map<GSExtensionUID, GSTranslationCacheList> cacheLists) {
		uidToVersion = new HashMap<>();
		
		for (Map.Entry<GSExtensionUID, GSTranslationCacheList> entry : cacheLists.entrySet())
			uidToVersion.put(entry.getKey(), entry.getValue().getVersion());
	}

	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		int n = buf.readInt();

		uidToVersion = new HashMap<>();
		while (n-- != 0) {
			GSExtensionUID uid = GSExtensionUID.read(buf);
			int version = buf.readInt();
			uidToVersion.put(uid, version);
		}
	}

	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		buf.writeInt(uidToVersion.size());
		
		for (Map.Entry<GSExtensionUID, Integer> entry : uidToVersion.entrySet()) {
			GSExtensionUID.write(buf, entry.getKey());
			buf.writeInt(entry.getValue().intValue());
		}
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
		controller.getTranslationModule().onTranslationVersionsReceived(player, uidToVersion);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller) {
	}
}
