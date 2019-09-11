package com.g4mesoft;

import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.tickspeed.GSITpsDependant;
import com.g4mesoft.tickspeed.GSTpsManagerClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class GSControllerClient extends GSController {

	private static final int INVALID_SERVER_VERSION = -1;
	
	private static final GSControllerClient instance = new GSControllerClient();
	
	private final MinecraftClient minecraft;
	private final GSTpsManagerClient tickspeedManager;
	
	private int serverVersion;
	
	public GSControllerClient() {
		minecraft = MinecraftClient.getInstance();
		tickspeedManager = new GSTpsManagerClient(this);
	
		serverVersion = INVALID_SERVER_VERSION;
	}
	
	public void keyReleased(int key, int scancode, int mods) {
		if (isInGame())
			tickspeedManager.keyReleased(key, scancode, mods);
	}

	public void keyPressed(int key, int scancode, int mods) {
		if (isInGame())
			tickspeedManager.keyPressed(key, scancode, mods);
	}

	public void keyRepeat(int key, int scancode, int mods) {
		if (isInGame())
			tickspeedManager.keyReleased(key, scancode, mods);
	}
	
	public boolean isInGame() {
		return minecraft.currentScreen == null;
	}

	public GSTpsManagerClient getTpsManager() {
		return tickspeedManager;
	}

	public void sendPacket(GSIPacket packet) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null)
			minecraft.getNetworkHandler().sendPacket(customPayload);
	}
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		SoundManager soundManager = minecraft.getSoundManager();
		((GSITpsDependant)soundManager).tpsChanged(newTps, oldTps);
	}
	
	@Override
	public Packet<?> encodeCustomPayload(Identifier identifier, PacketByteBuf buffer) {
		return new CustomPayloadC2SPacket(identifier, buffer);
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public int getVersion() {
		return G4mespeedMod.GS_VERSION;
	}

	public int getServerVersion() {
		return serverVersion;
	}
	
	public static GSControllerClient getInstance() {
		return instance;
	}
}
