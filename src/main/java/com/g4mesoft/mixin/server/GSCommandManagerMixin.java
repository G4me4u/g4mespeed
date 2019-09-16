package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSControllerServer;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(CommandManager.class)
public class GSCommandManagerMixin {
	
	@Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;
	
	@Inject(method="<init>", at = @At("RETURN"))
	private void registerCommands(boolean isServer, CallbackInfo ci) {
		GSControllerServer.getInstance().registerCommands(dispatcher);
	}
}
