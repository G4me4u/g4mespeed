package com.g4mesoft.module.tps;

import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.util.GSMathUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public final class GSTpsCommand {

	private static final double LOG_2 = Math.log(2.0);
	
	private GSTpsCommand() {
	}
	
	public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("tps").requires(context -> {
			return context.hasPermissionLevel(GSServerController.OP_PERMISSION_LEVEL);
		});
		
		builder.executes(context -> informCurrentTps(context.getSource()));
		
		builder.then(CommandManager.argument("newTps", FloatArgumentType.floatArg(GSTpsModule.MIN_TPS, GSTpsModule.MAX_TPS)).executes(context -> {
			return setCurrentTps(context.getSource(), FloatArgumentType.getFloat(context, "newTps"));
		}));
		
		dispatcher.register(builder);
	}
	
	private static int informCurrentTps(ServerCommandSource source) {
		float tps = GSServerController.getInstance().getTpsModule().getTps();
		String tpsFormatted = GSTpsModule.TPS_FORMAT.format(tps);
		
		float fn = (float)(Math.log(tps / GSTpsModule.DEFAULT_TPS) / LOG_2 * 12.0);
		int n = Math.round(fn);
		if (n % 12 != 0 && GSMathUtil.equalsApproximate(fn, n, 1E-4f)) {
			int o = n / 12;
			n %= 12;
			
			if (n < 0) {
				o--;
				n += 12;
			}
			
			if (o != 0) {
				source.sendFeedback(new TranslatableText("command.tps.geton", tpsFormatted, formatSign(o), formatSign(n)), false);
			} else {
				source.sendFeedback(new TranslatableText("command.tps.getn", tpsFormatted, formatSign(n)), false);
			}
		} else {
			source.sendFeedback(new TranslatableText("command.tps.get", tpsFormatted), false);
		}
		
		return Command.SINGLE_SUCCESS;
	}
	
	private static String formatSign(int value) {
		if (value > 0)
			return "+" + Integer.toString(value);
		return Integer.toString(value);
	}
	
	private static int setCurrentTps(ServerCommandSource source, float newTps) throws CommandSyntaxException {
		GSServerController.getInstance().getTpsModule().setTps(newTps);
		
		source.sendFeedback(new TranslatableText("command.tps.set", newTps), true);
		
		return Command.SINGLE_SUCCESS;
	}
}
