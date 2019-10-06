package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSControllerServer;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.UserCache;
import net.minecraft.world.level.LevelInfo;

@Mixin(IntegratedServer.class)
public class GSIntegratedServerMixin {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(MinecraftClient client, String string1, String string2, LevelInfo levelInfo, 
			YggdrasilAuthenticationService authService, MinecraftSessionService minecraftSessionService_1, 
			GameProfileRepository gameProfileRepository, UserCache userCache, 
			WorldGenerationProgressListenerFactory worldGenProgressListenerFactory, CallbackInfo ci) {
		
		GSControllerServer.getInstance().init((MinecraftServer) ((Object) this));
	}
}
