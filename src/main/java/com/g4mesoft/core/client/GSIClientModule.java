package com.g4mesoft.core.client;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public interface GSIClientModule extends GSIModule {

	@Override
	default public void init(GSIModuleManager manager) {
		if (!(manager instanceof GSIClientModuleManager))
			throw new UnsupportedOperationException();
		init((GSIClientModuleManager)manager);
	}
	
	/**
	 * Invoked during initialization of the <b>client</b>. This method is invoked <i>just
	 * before</i> the main game loop, and is only invoked once. A matching invocation of
	 * {@link #onClose()} always follows (unless the application is force terminated).
	 * 
	 * @param manager - the client manager which this module is installed on.
	 */
	public void init(GSIClientModuleManager manager);
	
	@Override
	default public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onPlayerJoin(ServerPlayer player) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onG4mespeedClientJoin(ServerPlayer player, GSExtensionInfo coreInfo) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onPlayerLeave(ServerPlayer player) {
		throw new UnsupportedOperationException();
	}

	@Override
	default public void onPlayerPermissionChanged(ServerPlayer player) {
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
