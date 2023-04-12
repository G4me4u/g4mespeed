package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.access.common.GSIServerPlayNetworkHandlerAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.minecraft.server.network.ServerPlayerEntity;

public class GSPlayerFixedMovementPacket implements GSIPacket {

	private boolean fixedMovement;
	
	public GSPlayerFixedMovementPacket() {
	}

	public GSPlayerFixedMovementPacket(boolean fixedMovement) {
		this.fixedMovement = fixedMovement;
	}

	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		fixedMovement = buf.readBoolean();
	}

	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		buf.writeBoolean(fixedMovement);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
		((GSIServerPlayNetworkHandlerAccess)player.networkHandler).gs_setFixedMovement(fixedMovement);
	}

	@Override
	public void handleOnClient(GSClientController controller) {
	}
}
