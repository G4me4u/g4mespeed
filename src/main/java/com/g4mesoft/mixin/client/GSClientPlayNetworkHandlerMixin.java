package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSIMinecraftClientAccess;
import com.g4mesoft.access.GSIRenderTickAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.packet.GSICustomPayloadHolder;
import com.g4mesoft.packet.GSIPacket;
import com.g4mesoft.packet.GSPacketManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.client.network.packet.WorldTimeUpdateS2CPacket;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(ClientPlayNetworkHandler.class)
public class GSClientPlayNetworkHandlerMixin {

	private static final int WORLD_TIME_UPDATE_INTERVAL = 20;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(CallbackInfo ci) {
		GSControllerClient.getInstance().setNetworkHandler((ClientPlayNetworkHandler)(Object) this);
	}

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		GSPacketManager packetManger = G4mespeedMod.getInstance().getPacketManager();
		GSIPacket gsPacket = packetManger.decodePacket((GSICustomPayloadHolder) packet);
		if (gsPacket != null) {
			gsPacket.handleOnClient(GSControllerClient.getInstance());
			ci.cancel();
		}
	}

	@Inject(method = "onWorldTimeUpdate", at = @At("HEAD"))
	public void onWorldTimeSync(WorldTimeUpdateS2CPacket worldTimePacket, CallbackInfo ci) {
		// Handled by GSServerSyncPacket
		if (GSControllerClient.getInstance().isG4mespeedServer())
			return;
		
		MinecraftClient client = MinecraftClient.getInstance();
		if (!client.isOnThread()) {
			RenderTickCounter counter = ((GSIMinecraftClientAccess)client).getRenderTickCounter();
			((GSIRenderTickAccess)counter).onServerTickSync(WORLD_TIME_UPDATE_INTERVAL);
		}
	}
}
