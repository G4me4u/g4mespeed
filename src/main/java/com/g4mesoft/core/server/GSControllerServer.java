package com.g4mesoft.core.server;

import java.io.File;
import java.util.function.Consumer;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.GSController;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSVersionPacket;
import com.g4mesoft.core.client.GSIModuleManagerClient;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class GSControllerServer extends GSController implements GSIModuleManagerServer, GSITpsDependant {

	public static final int OP_PERMISSION_LEVEL = 2;

	private static final GSControllerServer instance = new GSControllerServer();
	
	private MinecraftServer server;

	public GSControllerServer() {
		server = null;
	}
	
	@Override
	protected void initModules() {
		super.initModules();
		
		tpsModule.addTpsListener(this);
	}
	
	public void init(MinecraftServer server) {
		this.server = server;

		initModules();
	}

	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		for (GSIModule module : modules)
			module.registerCommands(dispatcher);
	}

	public void onPlayerJoin(ServerPlayerEntity player) {
		sendPacket(new GSVersionPacket(getVersion()), player, false);

		for (GSIModule module : modules)
			module.onPlayerJoin(player);
	}
	
	public void onG4mespeedClientJoined(ServerPlayerEntity player, int version) {
		((GSINetworkHandlerAccess)player.networkHandler).setG4mespeedInstalled(true);
		((GSINetworkHandlerAccess)player.networkHandler).setG4mespeedVersion(version);

		for (GSIModule module : modules)
			module.onG4mespeedClientJoin(player, version);
	}

	public void onPlayerLeave(ServerPlayerEntity player) {
		for (GSIModule module : modules)
			module.onPlayerLeave(player);
	}

	public void onServerShutdown() {
		for (GSIModule module : modules)
			module.onServerShutdown();
	}
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		if (server != null)
			((GSITpsDependant)server).tpsChanged(newTps, oldTps);
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

	@Override
	public void runOnClient(Consumer<GSIModuleManagerClient> consumer) {
	}

	@Override
	public void runOnServer(Consumer<GSIModuleManagerServer> consumer) {
		consumer.accept(this);
	}
	
	@Override
	public void sendPacket(GSIPacket packet, ServerPlayerEntity player) {
		sendPacket(packet, player, true);
	}
	
	@Override
	public void sendPacket(GSIPacket packet, ServerPlayerEntity player, boolean checkCompatibility) {
		if (checkCompatibility && !((GSINetworkHandlerAccess)player.networkHandler).isG4mespeedInstalled())
			return;
		
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null)
			player.networkHandler.sendPacket(customPayload);
	}

	@Override
	public void sendPacketToAll(GSIPacket packet) {
		sendPacketToAll(packet, true);
	}

	@Override
	public void sendPacketToAll(GSIPacket packet, boolean checkCompatibility) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		Packet<?> customPayload = packetManger.encodePacket(packet, this);
		if (customPayload != null) {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (!checkCompatibility || ((GSINetworkHandlerAccess)player.networkHandler).isG4mespeedInstalled())
					player.networkHandler.sendPacket(customPayload);
			}
		}
	}

	@Override
	public File getCacheFile() {
		return server.getRunDirectory();
	}
	
	public MinecraftServer getServer() {
		return server;
	}

	public static GSControllerServer getInstance() {
		return instance;
	}
}
