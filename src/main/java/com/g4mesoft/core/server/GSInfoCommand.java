package com.g4mesoft.core.server;

import java.util.Collections;
import java.util.List;

import com.g4mesoft.core.GSCoreExtension;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.util.math.BlockPos;

public class GSInfoCommand extends AbstractCommand {

	@Override
	public String getName() {
		return "gs";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getUsage(CommandSource source) {
		return "commands.gs.usage";
	}
	
	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource source, String[] args, BlockPos pos) {
		return args.length == 0 ? Collections.singletonList("info") : Collections.emptyList();
	}

	@Override
	public void run(MinecraftServer server, CommandSource source, String[] args) throws CommandException {
		if (args.length == 1 && "info".equals(args[0])) {
			// Format: /gs info
			sendSuccess(source, this, "command.gs.info", GSCoreExtension.VERSION);
		} else {
			throw new IncorrectUsageException(getUsage(source));
		}
	}
}
