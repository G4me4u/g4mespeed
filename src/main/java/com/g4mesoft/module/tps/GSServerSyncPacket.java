package com.g4mesoft.module.tps;

import java.io.IOException;

import com.g4mesoft.access.GSIMinecraftClientAccess;
import com.g4mesoft.access.GSIRenderTickAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.packet.GSIPacket;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class GSServerSyncPacket implements GSIPacket {

	public GSServerSyncPacket() {
	}
	
	@Override
	public void read(PacketByteBuf buf) throws IOException {
	}

	@Override
	public void write(PacketByteBuf buf) throws IOException {
	}

	@Override
	public void handleOnServer(GSControllerServer controller, ServerPlayerEntity player) {
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void handleOnClient(GSControllerClient controller) {
		RenderTickCounter counter = ((GSIMinecraftClientAccess)MinecraftClient.getInstance()).getRenderTickCounter();
		((GSIRenderTickAccess)counter).onServerTickSync(GSTpsModule.SERVER_SYNC_INTERVAL);
	}
	
	@Override
	public boolean shouldForceMainThread() {
		return false;
	}
}
