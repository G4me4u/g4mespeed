package com.g4mesoft.mixin.server;

import java.io.File;
import java.net.Proxy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.GSControllerServer;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.UserCache;

@Mixin(MinecraftServer.class)
public class GSMinecraftServerMixin {

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	private void onMinecraftServerCTOR(File file, Proxy proxy, DataFixer dataFixer, CommandManager commandManager, 
			YggdrasilAuthenticationService authService, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo,
			UserCache userCache, WorldGenerationProgressListenerFactory worldGenProgressListenerFactory, String string, CallbackInfo ci) {
		
		GSControllerServer.getInstance().init((MinecraftServer)((Object)this));
	}
}
