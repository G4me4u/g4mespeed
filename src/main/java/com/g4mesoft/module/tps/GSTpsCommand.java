package com.g4mesoft.module.tps;

import com.g4mesoft.core.server.GSControllerServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class GSTpsCommand {

	public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("tps").requires((context) -> {
			return context.hasPermissionLevel(GSControllerServer.OP_PERMISSION_LEVEL);
		});
		
		builder.executes((context) -> {
			return informCurrentTps(context.getSource());
		});
		
		builder.then(CommandManager.argument("newTps", FloatArgumentType.floatArg(GSTpsModule.MIN_TPS, GSTpsModule.MAX_TPS)).executes((context) -> {
			return setCurrentTps(context.getSource(), FloatArgumentType.getFloat(context, "newTps"));
		}));
		
		dispatcher.register(builder);
	}
	
	private static int informCurrentTps(ServerCommandSource source) {
		float tps = GSControllerServer.getInstance().getTpsModule().getTps();
		source.sendFeedback(new TranslatableText("command.tps.get", tps), false);
		return 1;
	}
	
	private static int setCurrentTps(ServerCommandSource source, float newTps) throws CommandSyntaxException {
		GSControllerServer.getInstance().getTpsModule().setTps(newTps);
		source.sendFeedback(new TranslatableText("command.tps.set", newTps), true);
		return 1;
	}
}
