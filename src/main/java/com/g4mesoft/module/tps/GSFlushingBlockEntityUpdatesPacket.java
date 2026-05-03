package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.access.client.GSIMinecraftAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

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
	public void handleOnServer(GSServerController controller, ServerPlayer player) {
	}

	@Override
	public void handleOnClient(GSClientController controller) {
		((GSIMinecraftAccess)Minecraft.getInstance()).gs_setFlushingBlockEntityUpdates(flushingUpdates);
	}
}
