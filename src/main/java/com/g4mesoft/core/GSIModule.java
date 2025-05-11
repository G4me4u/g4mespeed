package com.g4mesoft.core;

import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.core.client.GSIClientModuleManager;
import com.g4mesoft.core.server.GSIServerModuleManager;
import com.g4mesoft.gui.GSTabbedGUI;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.setting.GSSettingManager;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface GSIModule {

	/**
	 * Invoked during initialization of the <b>client and server</b>. On both the
	 * client and server this method is invoked <i>just before</i> the main game loop.
	 * The client only invokes this method once, but the server might invoke this
	 * method several times, but not before a matching invocation of {@link #onClose()}.
	 * 
	 * @param manager - the manager which this module is installed on.
	 */
	public void init(GSIModuleManager manager);

	/**
	 * Invoked on the <b>client</b> just after initialization to allow modules to add
	 * their own tabs to the primary G4mespeed tabbed GUI. This method is <i>not</i>
	 * invoked on the server.
	 * 
	 * @param tabbedGUI - the primary tabbed gui panel
	 */
	@Environment(EnvType.CLIENT)
	default public void initGUI(GSTabbedGUI tabbedGUI) { }
	
	/**
	 * Invoked during termination of the <b>client and server</b>. On the client this
	 * method is invoked at the beginning of the {@link MinecraftClient#stop()} method,
	 * and on the server it is invoked at the end of {@link MinecraftServer#shutdown()}.
	 * <br><br>
	 * <i>Note: an invocation will only happen to this method if there is a matching
	 *          prior call to {@link #init(GSIModuleManager)}. The client only invokes
	 *          this method once.</i>
	 */
	default public void onClose() { }

	/**
	 * Invoked on the <b>client</b> after initialization to allow modules to register
	 * their own settings. This method is <i>not</i> invoked on the server.
	 * 
	 * @param settings - the client settings manager
	 */
	@Environment(EnvType.CLIENT)
	default public void registerClientSettings(GSSettingManager settings) { }


	/**
	 * Invoked on the <b>client</b> after initialization to allow modules to register
	 * their own hotkeys/keybinds. This method is <i>not</i> invoked on the server.
	 * 
	 * @param keyManager - the client key manager
	 */
	@Environment(EnvType.CLIENT)
	default public void registerHotkeys(GSKeyManager keyManager) { }

	/**
	 * Invoked on the <b>client and server</b> after initialization to allow modules to
	 * register their global (across worlds) settings. On the client this will register
	 * <i>shadow</i> settings that will automatically be updated to match the settings
	 * stored on the server and vice versa if the client changes them.
	 * 
	 * @param settings - the global server settings manager, or a remote shadow setting
	 *                   manager when invoked from the client.
	 */
	default public void registerGlobalServerSettings(GSSettingManager settings) {
		registerServerSettings(settings);
	}

	/**
	 * Invoked on the <b>client and server</b> after initialization to allow modules to
	 * register their world specific settings. On the client this will register <i>shadow</i>
	 * settings that will automatically be updated to match the settings stored on the
	 * server and vice versa if the client changes them.
	 * 
	 * @param settings - the world server settings manager, or a remote shadow setting
	 *                   manager when invoked from the client.
	 */
	default public void registerWorldServerSettings(GSSettingManager settings) { }

	/**
	 * @deprecated Replaced by {@link #registerGlobalServerSettings(GSSettingManager)}.
	 * 
	 * @param settings - the setting manager for global settings.
	 */
	@Deprecated
	default public void registerServerSettings(GSSettingManager settings) { }

	/**
	 * Invoked on the <b>server</b> after initialization to allow modules to register
	 * their own commands. This method is <i>not</i> invoked on the client.
	 * 
	 * @param dispatcher - the server command dispatcher
	 */
	default public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) { }

	/**
	 * Invoked at the beginning of a <b>client and server</b> tick, depending on whether
	 * this module is installed on a client- or server module manager, respectively.
	 * 
	 * @param paused - whether the client is currently paused. On integrated servers this
	 *                 has the same value as on the client, but dedicated (and LAN) servers
	 *                 always have this as false.
	 */
	default public void tick(boolean paused) { }
	
	/**
	 * Invoked on the <b>client</b> when the player receives the world state from the server.
	 * <br><br>
	 * <i>Note: at the invocation of this method, the client has no knowledge of extensions
	 *          installed on the server. Therefore, one <b>can not send packets or check if
	 *          G4mespeed is installed</b> at this stage. These actions should only be performed
	 *          once the {@link #onJoinG4mespeedServer(GSExtensionInfo)} has been invoked.</i>
	 */
	@Environment(EnvType.CLIENT)
	default public void onJoinServer() { }

	/**
	 * Invoked on the <b>client</b> after the player has received the world state from the server
	 * and has received knowledge about G4mespeed extensions installed on the server. That is,
	 * once the client has verified that the server has an installation of G4mespeed. After this,
	 * {@link GSIClientModuleManager#isServerExtensionInstalled(com.g4mesoft.GSExtensionUID)} and
	 * other related methods will return accurate results according to their function.
	 * 
	 * @param coreInfo - the core extension info
	 */
	@Environment(EnvType.CLIENT)
	default public void onJoinG4mespeedServer(GSExtensionInfo coreInfo) { }

	/**
	 * Invoked on the <b>client</b> after the player has disconnected from the server. In particular,
	 * this is invoked just before the disconnect screen occurs.
	 */
	@Environment(EnvType.CLIENT)
	default public void onDisconnectServer() { }

	/**
	 * Invoked on the <b>server</b> when a player joins the world.
	 * <br><br>
	 * <i>Note: at the invocation of this method, the server has no knowledge of extensions
	 *          installed on the client. Therefore, one <b>can not send packets to or check if
	 *          G4mespeed is installed</b> at this stage. These actions should only be performed
	 *          once the {@link #onG4mespeedClientJoin(ServerPlayerEntity, GSExtensionInfo)}
	 *          has been invoked.</i>
	 * 
	 * @param player
	 */
	default public void onPlayerJoin(ServerPlayerEntity player) { }

	/**
	 * Invoked on the <b>server</b> after receiving knowledge about G4mespeed extensions that are
	 * installed on the client of the player. That is, once the server has received a packet with
	 * extension info which verifies that the client has an installation of G4mespeed. After this,
	 * {@link GSIServerModuleManager#isExtensionInstalled(ServerPlayerEntity, com.g4mesoft.GSExtensionUID)}
	 * and other related methods will return accurate results according to their function.
	 * 
	 * @param player - the player who is confirmed to have G4mespeed installed.
	 * @param coreInfo - the core extension info
	 */
	default public void onG4mespeedClientJoin(ServerPlayerEntity player, GSExtensionInfo coreInfo) { }

	/**
	 * Invoked on the <b>server</b> after a player has left.
	 * 
	 * @param player - the player who left the server
	 */
	default public void onPlayerLeave(ServerPlayerEntity player) { }

	/**
	 * Invoked on the <b>server</b> whenever the permission level of a player changes.
	 * 
	 * @param player - the player whose permission changed.
	 */
	default public void onPlayerPermissionChanged(ServerPlayerEntity player) { }

	/**
	 * @return True if this module is client-side and can be installed on a
	 *         {@link GSIClientModuleManager}. False otherwise.
	 */
	default public boolean isClientSide() {
		return true;
	}

	/**
	 * @return True if this module is server-side and can be installed on a
	 *         {@link GSIServerModuleManager}. False otherwise.
	 */
	default public boolean isServerSide() {
		return true;
	}
}
