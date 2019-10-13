package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSTpsHotkeyPacket implements GSIPacket {

	private GSETpsHotkeyType type;
	private boolean sneaking;
	
	public GSTpsHotkeyPacket() {
	}
	
	public GSTpsHotkeyPacket(GSETpsHotkeyType type, boolean sneaking) {
		this.type = type;
		this.sneaking = sneaking;
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
		type = GSETpsHotkeyType.fromIndex((int)buf.readByte());
		sneaking = buf.readBoolean();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeByte((byte)type.getIndex());
		buf.writeBoolean(sneaking);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		GSTpsModule tpsModule = controller.getTpsModule();
		if (tpsModule.isPlayerAllowedTpsChange(player))
			tpsModule.performHotkeyAction(type, sneaking);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		controller.getTpsModule().performHotkeyAction(type, sneaking);
	}
}
