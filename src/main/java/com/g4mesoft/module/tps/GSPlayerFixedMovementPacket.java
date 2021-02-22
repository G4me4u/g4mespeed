package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.access.GSIServerPlayNetworkHandlerAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSPlayerFixedMovementPacket implements GSIPacket {

	private boolean fixedMovement;
	
	public GSPlayerFixedMovementPacket() {
	}

	public GSPlayerFixedMovementPacket(boolean fixedMovement) {
		this.fixedMovement = fixedMovement;
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		fixedMovement = buf.readBoolean();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeBoolean(fixedMovement);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		((GSIServerPlayNetworkHandlerAccess)player.networkHandler).setFixedMovement(fixedMovement);
	}

	@Override
	public void handleOnClient(GSControllerClient controller) {
	}
}
