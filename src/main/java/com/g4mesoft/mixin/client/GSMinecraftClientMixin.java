package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.GSControllerClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public class GSMinecraftClientMixin {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(CallbackInfo ci) {
		GSControllerClient.getInstance().setMinecraftClient((MinecraftClient)(Object)this);
	}
	
	@Inject(method = "disconnect", at = @At("HEAD"))
	public void onDisconnect(CallbackInfo ci) {
		GSControllerClient.getInstance().onDisconnectServer();
	}
}
