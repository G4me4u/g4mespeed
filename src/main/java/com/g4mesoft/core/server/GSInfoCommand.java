package com.g4mesoft.core.server;

import com.g4mesoft.core.GSCoreExtension;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class GSInfoCommand {

	private GSInfoCommand() {
	}

	public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("gs").then(Commands.literal("info").executes(context -> {
			return informCoreVersion(context.getSource());
		})));
	}

	private static int informCoreVersion(CommandSourceStack source) {
		source.sendSuccess(() -> Component.translatable("command.gs.info", GSCoreExtension.VERSION.toString()), false);
		
		return Command.SINGLE_SUCCESS;
	}
}
