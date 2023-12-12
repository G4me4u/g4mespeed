package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.minecraft.server.network.ServerPlayerEntity;

public class GSTickSprintUpdatePacket implements GSIPacket {

	private boolean sprinting;
	
	public GSTickSprintUpdatePacket() {
	}

	public GSTickSprintUpdatePacket(boolean sprinting) {
		this.sprinting = sprinting;
	}
	
	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		buf.writeBoolean(sprinting);
	}
	
	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		sprinting = buf.readBoolean();
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
	}

	@Override
	public void handleOnClient(GSClientController controller) {
		controller.getTpsModule().onTickSprintChanged(sprinting);
	}
}
