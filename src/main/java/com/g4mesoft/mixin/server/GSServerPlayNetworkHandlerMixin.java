package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSINetworkHandlerAccess;
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

	private boolean gsInstalled = false;
	private int gsVersion = G4mespeedMod.INVALID_GS_VERSION;

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
	public void setG4mespeedInstalled(boolean gsInstalled) {
		this.gsInstalled = gsInstalled;
	}

	@Override
	public boolean isG4mespeedInstalled() {
		return gsInstalled;
	}

	@Override
	public void setG4mespeedVersion(int gsVersion) {
		this.gsVersion = gsVersion;
	}

	@Override
	public int getG4mespeedVersion() {
		return gsVersion;
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
