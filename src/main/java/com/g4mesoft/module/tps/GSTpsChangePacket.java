package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

public class GSTpsChangePacket implements GSIPacket {

	private float requestedTps;
	
	public GSTpsChangePacket() {
	}
	
	public GSTpsChangePacket(float requestedTps) {
		this.requestedTps = requestedTps;
	}
	
	@Override
	public void read(GSDecodeBuffer buf) throws IOException {
		requestedTps = buf.readFloat();
	}

	@Override
	public void write(GSEncodeBuffer buf) throws IOException {
		buf.writeFloat(requestedTps);
	}

	@Override
	public void handleOnServer(GSServerController controller, ServerPlayerEntity player) {
		GSTpsModule tpsModule = controller.getTpsModule();
		if (tpsModule.isPlayerAllowedTpsChange(player))
			controller.getTpsModule().setTps(requestedTps);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSClientController controller) {
		controller.getTpsModule().setTps(requestedTps);
	}
}
