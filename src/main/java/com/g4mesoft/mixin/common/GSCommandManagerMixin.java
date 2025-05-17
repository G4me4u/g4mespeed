package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSServerController;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.server.command.source.CommandSourceStack;

@Mixin(CommandManager.class)
public class GSCommandManagerMixin {
	
	@Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;
	
	@Inject(
		method="<init>",
		at = @At("RETURN")
	)
	private void registerCommands(boolean isServer, CallbackInfo ci) {
		GSServerController.getInstance().setCommandDispatcher(dispatcher);
	}
}
