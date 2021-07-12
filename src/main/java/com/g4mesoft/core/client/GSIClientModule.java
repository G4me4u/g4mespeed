package com.g4mesoft.core.client;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIClientModule extends GSIModule {

	@Override
	default public void init(GSIModuleManager manager) {
		if (!(manager instanceof GSIClientModuleManager))
			throw new UnsupportedOperationException();
		init((GSIClientModuleManager)manager);
	}
	
	public void init(GSIClientModuleManager manager);
	
	@Override
	default public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onPlayerJoin(ServerPlayerEntity player) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onG4mespeedClientJoin(ServerPlayerEntity player, GSExtensionInfo coreInfo) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onPlayerLeave(ServerPlayerEntity player) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onPlayerPermissionChanged(ServerPlayerEntity player) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	default public boolean isClientSide() {
		return true;
	}

	@Override
	default public boolean isServerSide() {
		return false;
	}
}
