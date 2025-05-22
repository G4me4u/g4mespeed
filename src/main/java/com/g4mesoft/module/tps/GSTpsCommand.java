package com.g4mesoft.module.tps;

import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.ui.util.GSMathUtil;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;

public final class GSTpsCommand extends AbstractCommand {

	private static final double LOG_2 = Math.log(2.0);
	
	private final GSTpsModule tpsModule;
	
	public GSTpsCommand(GSTpsModule tpsModule) {
		this.tpsModule = tpsModule;
	}
	
	@Override
	public String getName() {
		return "tps";
	}

	@Override
	public int getRequiredPermissionLevel() {
		if (GSServerController.getInstance().getTpsModule().sRequireOP.get())
			return GSServerController.OP_PERMISSION_LEVEL;
		return 0;
	}
	
	@Override
	public String getUsage(CommandSource source) {
		return "commands.tps.usage";
	}

	@Override
	public void run(MinecraftServer server, CommandSource source, String[] args) throws CommandException {
		if (args.length == 0) {
			// Format: /tps
			informCurrentTps(source);
		} else if (args.length == 1) {
			// Format: /tps <newTps>
			float newTps = (float)parseDouble(args[0], GSTpsModule.MIN_TPS, GSTpsModule.MAX_TPS);
			setCurrentTps(source, newTps);
		} else {
			throw new IncorrectUsageException(getUsage(source));
		}
	}
	
	private static String formatSign(int value) {
		if (value > 0)
			return "+" + Integer.toString(value);
		return Integer.toString(value);
	}
	
	private void informCurrentTps(CommandSource source) {
		float tps = tpsModule.getTps();
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
				sendSuccess(source, this, "command.tps.geton", tpsFormatted, formatSign(o), formatSign(n));
			} else {
				sendSuccess(source, this, "command.tps.getn", tpsFormatted, formatSign(n));
			}
		} else {
			sendSuccess(source, this, "command.tps.get", tpsFormatted);
		}
	}
	
	private void setCurrentTps(CommandSource source, float newTps) throws CommandException {
		tpsModule.setTps(newTps);
		
		sendSuccess(source, this, "command.tps.set", newTps);
	}
}
