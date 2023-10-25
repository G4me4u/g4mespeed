package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.common.GSIServerPlayNetworkHandlerAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerCommonNetworkHandler.class)
public class GSServerCommonNetworkHandlerMixin {

	@Shadow @Final protected MinecraftServer server;
	
	@Inject(
		method = "onCustomPayload",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onCustomPayload(CustomPayloadC2SPacket customPayloadPacket, CallbackInfo ci) {
		if (!(this instanceof GSIServerPlayNetworkHandlerAccess)) {
			// We only accept packets during play.
			return;
		}
		GSIServerPlayNetworkHandlerAccess access = (GSIServerPlayNetworkHandlerAccess)this;
		
		GSPacketManager packetManger = G4mespeedMod.getPacketManager();
		GSIPacket packet = packetManger.decodePacket(customPayloadPacket.payload(), access.gs_getExtensionInfoList());
		if (packet != null) {
			packetManger.handlePacket(packet, (ServerPlayNetworkHandler)(Object)this, server, p -> {
				p.handleOnServer(GSServerController.getInstance(), access.gs_getPlayer());
			});
			ci.cancel();
		}
	}
}
