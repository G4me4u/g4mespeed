package com.g4mesoft.module.translation;

import java.io.IOException;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class GSTranslationVersionPacket implements GSIPacket {

	private int translationVersion;
	
	public GSTranslationVersionPacket() {
	}
	
	public GSTranslationVersionPacket(int translationVersion) {
		this.translationVersion = translationVersion;
	}

	@Override
	public void read(PacketByteBuf buf) throws IOException {
		translationVersion = buf.readInt();
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
		buf.writeInt(translationVersion);
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
		controller.getTranslationModule().onTranslationVersionReceived(player, translationVersion);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
	}
}
