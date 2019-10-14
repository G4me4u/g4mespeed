package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.module.tps.GSITpsDependant;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;

@Mixin(IntegratedServer.class)
public class GSIntegratedServerMixin {

	@Inject(method = "setupServer", at = @At("RETURN"))
	public void onInit(CallbackInfoReturnable<Boolean> cir) {
		GSControllerServer controllerServer = GSControllerServer.getInstance();
		controllerServer.init((MinecraftServer)((Object)this));
		
		controllerServer.getTpsModule().addTpsListener((GSITpsDependant)this);
	}
}
