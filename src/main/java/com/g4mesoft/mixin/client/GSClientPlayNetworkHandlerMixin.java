package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSControllerClient;
import com.g4mesoft.packet.GSICustomPayloadHolder;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
@Environment(EnvType.CLIENT)
public class GSClientPlayNetworkHandlerMixin {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(CallbackInfo ci) {
		GSControllerClient.getInstance().setNetworkHandler((ClientPlayNetworkHandler)(Object)this);
	}
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		GSIPacket gsPacket = packetManger.decodePacket((GSICustomPayloadHolder)packet);
		if (gsPacket != null) {
			gsPacket.handleOnClient(GSControllerClient.getInstance());
			ci.cancel();
		}
	}
}
