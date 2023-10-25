package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

@Mixin(ClientCommonNetworkHandler.class)
public class GSClientCommonNetworkHandlerMixin {

	@Shadow @Final protected MinecraftClient client;
	
	@Inject(
		method = "onCustomPayload",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onCustomPayload(CustomPayloadS2CPacket customPayloadPacket, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getPacketManager();
		GSClientController controllerClient = GSClientController.getInstance();
		GSIPacket packet = packetManger.decodePacket(customPayloadPacket.payload(), controllerClient.getServerExtensionInfoList());
		if (packet != null) {
			packetManger.handlePacket(packet, (ClientPlayNetworkHandler)(Object)this, client, p -> {
				p.handleOnClient(GSClientController.getInstance());
			});
			ci.cancel();
		}
	}
}
