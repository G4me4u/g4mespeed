package com.g4mesoft.core.server;

import com.g4mesoft.core.GSCoreExtension;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.server.command.source.CommandSourceStack;
import net.minecraft.text.TranslatableText;

public final class GSInfoCommand {

	private GSInfoCommand() {
	}

	public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(CommandManager.literal("gs").then(CommandManager.literal("info").executes(context -> {
			return informCoreVersion(context.getSource());
		})));
	}

	private static int informCoreVersion(CommandSourceStack source) {
		source.sendSuccess(new TranslatableText("command.gs.info", GSCoreExtension.VERSION), false);
		
		return Command.SINGLE_SUCCESS;
	}
}
