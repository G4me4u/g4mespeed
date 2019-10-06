package com.g4mesoft.mixin.server;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSControllerServer;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.util.UserCache;

@Mixin(MinecraftDedicatedServer.class)
public class GSMinecraftDedicatedServerMixin {

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(File file, ServerPropertiesLoader propertiesLoader, DataFixer dataFixer, 
			YggdrasilAuthenticationService authService, MinecraftSessionService sessionService, 
			GameProfileRepository gameProfileRepo, UserCache userCache, 
			WorldGenerationProgressListenerFactory worldGenProgressListenerFactory, String string_1, CallbackInfo ci) {
		
		GSControllerServer.getInstance().init((MinecraftServer) ((Object) this));
	}
}
