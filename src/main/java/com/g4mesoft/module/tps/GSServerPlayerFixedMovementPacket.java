package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.access.client.GSIAbstractClientPlayerAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

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
	public void handleOnServer(GSServerController controller, ServerPlayer player) {
	}

	@Override
	public void handleOnClient(GSClientController controller) {
		Minecraft client = Minecraft.getInstance();
		if (client.level != null) {
			Entity entity = client.level.getEntity(entityId);
			if (entity instanceof AbstractClientPlayer)
				((GSIAbstractClientPlayerAccess)entity).gs_setFixedMovement(fixedMovement);
		}
	}
}
