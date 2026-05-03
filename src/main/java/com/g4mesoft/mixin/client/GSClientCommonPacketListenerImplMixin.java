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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

@Mixin(ClientCommonPacketListenerImpl.class)
public class GSClientCommonPacketListenerImplMixin {

	@Shadow @Final protected Minecraft minecraft;
	
	@Inject(
		method = "handleCustomPayload",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onHandleCustomPayload(ClientboundCustomPayloadPacket customPayloadPacket, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getPacketManager();
		GSClientController controllerClient = GSClientController.getInstance();
		GSIPacket packet = packetManger.decodePacket(customPayloadPacket.payload(), controllerClient.getServerExtensionInfoList());
		if (packet != null) {
			packetManger.handlePacket(packet, (ClientPacketListener)(Object)this, minecraft, p -> {
				p.handleOnClient(GSClientController.getInstance());
			});
			ci.cancel();
		}
	}
}
