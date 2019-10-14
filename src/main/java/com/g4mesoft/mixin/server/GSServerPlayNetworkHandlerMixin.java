package com.g4mesoft.mixin.server;

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
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;

@Mixin(ServerPlayNetworkHandler.class)
public class GSServerPlayNetworkHandlerMixin implements GSINetworkHandlerAccess {

	private GSVersion version = GSVersion.INVALID;

	private int translationVersion = GSTranslationModule.INVALID_TRANSLATION_VERSION;
	
	@Shadow public ServerPlayerEntity player;
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		
		@SuppressWarnings("unchecked")
		GSICustomPayloadHolder<ServerPlayPacketListener> payload = (GSICustomPayloadHolder<ServerPlayPacketListener>)packet;
		
		GSControllerServer controllerServer = GSControllerServer.getInstance();
		GSIPacket gsPacket = packetManger.decodePacket(payload, (ServerPlayNetworkHandler)(Object)this, controllerServer.getServer());
		if (gsPacket != null) {
			gsPacket.handleOnServer(controllerServer, player);
			ci.cancel();
		}
	}
	
	@Override
	public void setG4mespeedVersion(GSVersion version) {
		this.version = version;
	}

	@Override
	public GSVersion getG4mespeedVersion() {
		return version;
	}

	@Override
	public void setTranslationVersion(int translationVersion) {
		this.translationVersion = translationVersion;
	}

	@Override
	public int getTranslationVersion() {
		return translationVersion;
	}
}
