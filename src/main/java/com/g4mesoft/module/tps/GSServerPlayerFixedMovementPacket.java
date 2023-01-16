package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.access.client.GSIAbstractClientPlayerEntityAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSServerPlayerFixedMovementPacket implements GSIPacket {

	private int entityId;
	private boolean fixedMovement;
	
	public GSServerPlayerFixedMovementPacket() {
	}

	public GSServerPlayerFixedMovementPacket(int entityId, boolean fixedMovement) {
		this.entityId = entityId;
		this.fixedMovement = fixedMovement;
	}
	
	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		entityId = buf.readInt();
		fixedMovement = buf.readBoolean();
	}

	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		buf.writeInt(entityId);
		buf.writeBoolean(fixedMovement);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
	}

	@Override
	public void handleOnClient(GSClientController controller) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null) {
			Entity entity = client.world.getEntityById(entityId);
			if (entity instanceof AbstractClientPlayerEntity)
				((GSIAbstractClientPlayerEntityAccess)entity).gs_setFixedMovement(fixedMovement);
		}
	}
}
