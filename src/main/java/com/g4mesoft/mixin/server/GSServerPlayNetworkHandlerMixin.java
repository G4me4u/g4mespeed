package com.g4mesoft.mixin.server;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.GSExtensionInfoList;
import com.g4mesoft.GSExtensionUID;
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

	@Shadow public ServerPlayerEntity player;

	private final GSExtensionInfoList extensionInfoList = new GSExtensionInfoList();
	private final Map<GSExtensionUID, Integer> translationVersions = new HashMap<>();
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadHolder<ServerPlayPacketListener> payload = (GSICustomPayloadHolder<ServerPlayPacketListener>)packet;
		
		GSControllerServer controllerServer = GSControllerServer.getInstance();
		GSIPacket gsPacket = packetManger.decodePacket(payload, extensionInfoList, (ServerPlayNetworkHandler)(Object)this, controllerServer.getServer());
		if (gsPacket != null) {
			gsPacket.handleOnServer(controllerServer, player);
			ci.cancel();
		}
	}
	
	@Override
	public boolean isExtensionInstalled(GSExtensionUID extensionUid) {
		return extensionInfoList.isExtensionInstalled(extensionUid);
	}
	
	@Override
	public boolean isExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion) {
		return extensionInfoList.isExtensionInstalled(extensionUid, minimumVersion);
	}

	@Override
	public GSExtensionInfo getExtensionInfo(GSExtensionUID extensionUid) {
		return extensionInfoList.getInfo(extensionUid);
	}

	@Override
	public void clearAllExtensionInfo() {
		extensionInfoList.clearInfo();
	}
	
	@Override
	public void addAllExtensionInfo(GSExtensionInfo[] extensionInfo) {
		extensionInfoList.addAllInfo(extensionInfo);
	}

	@Override
	public void addExtensionInfo(GSExtensionInfo info) {
		extensionInfoList.addInfo(info);
	}
	
	@Override
	public void setTranslationVersion(GSExtensionUID uid, int translationVersion) {
		translationVersions.put(uid, translationVersion);
	}

	@Override
	public int getTranslationVersion(GSExtensionUID uid) {
		return translationVersions.getOrDefault(uid, GSTranslationModule.INVALID_TRANSLATION_VERSION);
	}
}
