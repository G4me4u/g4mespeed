package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.access.client.GSIMinecraftClientAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSFlushingBlockEntityUpdatesPacket implements GSIPacket {

	private boolean flushingUpdates;
	
	public GSFlushingBlockEntityUpdatesPacket() {
	}

	public GSFlushingBlockEntityUpdatesPacket(boolean flushingUpdates) {
		this.flushingUpdates = flushingUpdates;
	}
	
	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		flushingUpdates = buf.readBoolean();
	}

	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		buf.writeBoolean(flushingUpdates);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
	}

	@Override
	public void handleOnClient(GSClientController controller) {
		((GSIMinecraftClientAccess)MinecraftClient.getInstance()).gs_setFlushingBlockEntityUpdates(flushingUpdates);
	}
}
