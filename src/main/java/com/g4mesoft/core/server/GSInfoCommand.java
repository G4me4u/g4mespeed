package com.g4mesoft.core.server;

import com.g4mesoft.core.GSCoreExtension;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class GSInfoCommand {

	private GSInfoCommand() {
	}

	public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("gs").then(CommandManager.literal("info").executes(context -> {
			return informCoreVersion(context.getSource());
		})));
	}

	private static int informCoreVersion(ServerCommandSource source) {
		source.sendFeedback(Text.translatable("command.gs.info", GSCoreExtension.VERSION), false);
		
		return Command.SINGLE_SUCCESS;
	}
}
