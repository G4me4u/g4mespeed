package com.g4mesoft.mixin.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.packet.GSICustomPayloadHolder;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public class GSServerPlayNetworkHandlerMixin implements GSINetworkHandlerAccess {

	private GSVersion version = GSVersion.INVALID;

	private byte[] extensionUids = new byte[0];
	private final Set<Byte> extensionUidSet = new HashSet<Byte>();
	
	private Map<Byte, Integer> translationVersions = new HashMap<Byte, Integer>();
	
	@Shadow public ServerPlayerEntity player;
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadHolder<ServerPlayPacketListener> payload = (GSICustomPayloadHolder<ServerPlayPacketListener>)packet;
		
		GSControllerServer controllerServer = GSControllerServer.getInstance();
		GSIPacket gsPacket = packetManger.decodePacket(payload, version, (ServerPlayNetworkHandler)(Object)this, controllerServer.getServer());
		if (gsPacket != null) {
			gsPacket.handleOnServer(controllerServer, player);
			ci.cancel();
		}
	}
	
	@Override
	public void setCoreVersion(GSVersion version) {
		this.version = version;
	}

	@Override
	public GSVersion getCoreVersion() {
		return version;
	}

	@Override
	public void setExtensionUids(byte[] extensionUids) {
		this.extensionUids = Arrays.copyOf(extensionUids, extensionUids.length);
	
		extensionUidSet.clear();
		for (byte uid : this.extensionUids)
			extensionUidSet.add(uid);
	}
	
	@Override
	public byte[] getExtensionUids() {
		return extensionUids;
	}
	
	@Override
	public boolean isExtensionInstalled(byte extensionUid) {
		return extensionUidSet.contains(extensionUid);
	}
	
	@Override
	public void setTranslationVersion(byte uid, int translationVersion) {
		translationVersions.put(uid, translationVersion);
	}

	@Override
	public int getTranslationVersion(byte uid) {
		return translationVersions.getOrDefault(uid, GSTranslationModule.INVALID_TRANSLATION_VERSION);
	}
}
