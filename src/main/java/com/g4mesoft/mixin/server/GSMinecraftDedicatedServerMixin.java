package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.tps.GSITpsDependant;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

@Mixin(MinecraftDedicatedServer.class)
public class GSMinecraftDedicatedServerMixin {

	@Inject(method = "setupServer", at = @At("RETURN"))
	public void onInit(CallbackInfoReturnable<Boolean> cir) {
		GSControllerServer controllerServer = GSControllerServer.getInstance();
		controllerServer.init((MinecraftServer)((Object)this));
		
		controllerServer.getTpsModule().addTpsListener((GSITpsDependant)this);
	}
}
