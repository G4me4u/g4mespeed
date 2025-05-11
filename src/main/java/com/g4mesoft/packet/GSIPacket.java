package com.g4mesoft.packet;

import java.io.IOException;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIPacket {

	public void read(GSDecodeBuffer buf) throws IOException;

	default public void read(GSDecodeBuffer buf, GSExtensionInfo extensionInfo) throws IOException {
		read(buf);
	}

	public void write(GSEncodeBuffer buf) throws IOException;
	
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player);

	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller);

	default public boolean shouldForceMainThread() {
		return true;
	}
}
