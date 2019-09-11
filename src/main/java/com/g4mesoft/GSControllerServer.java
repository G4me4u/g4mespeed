package com.g4mesoft;

import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.g4mesoft.tickspeed.GSTpsChangePacket;
import com.g4mesoft.tickspeed.GSTpsManagerServer;

import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class GSControllerServer extends GSController {

	private static final GSControllerServer instance = new GSControllerServer();
	
	private MinecraftServer server;
	private final GSTpsManagerServer tpsManager;
	
	public GSControllerServer() {
		server = null;
		
		tpsManager = new GSTpsManagerServer(this);
	}
	
	public void init(MinecraftServer server) {
		this.server = server;
	}

	public void onPlayerJoin(ServerPlayerEntity player) {
		// TODO: make this work....
	}

	public void onPlayerLeave(ServerPlayerEntity player) {
	}
	
	public void sendPacketToAll(GSIPacket packet) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				// TODO: add check if g4mespeed is supported!
				player.networkHandler.sendPacket(customPayload);
			}
		}
	}

	public GSTpsManagerServer getTpsManager() {
		return tpsManager;
	}
	
	public MinecraftServer getServer() {
		return server;
	}
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		sendPacketToAll(new GSTpsChangePacket(newTps));
	}

	@Override
	public Packet<?> encodeCustomPayload(Identifier identifier, PacketByteBuf buffer) {
		return new CustomPayloadS2CPacket(identifier, buffer);
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public int getVersion() {
		return G4mespeedMod.GS_VERSION;
	}

	public static GSControllerServer getInstance() {
		return instance;
	}
}
