package com.g4mesoft.core.server;

import com.g4mesoft.G4mespeedMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public final class GSInfoCommand {

	public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("gs").then(CommandManager.literal("info").executes(context -> {
			return informG4mespeedVersion(context.getSource());
		})));
	}

	private static int informG4mespeedVersion(ServerCommandSource source) {
		source.sendFeedback(new TranslatableText("command.gs.info", G4mespeedMod.GS_CORE_VERSION), false);
		
		return Command.SINGLE_SUCCESS;
	}
}
