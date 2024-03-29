package com.g4mesoft.module.tps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.access.client.GSIAbstractClientPlayerEntityAccess;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.client.GSIClientModuleManager;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.core.compat.GSICarpetTickrateListener;
import com.g4mesoft.core.compat.GSICarpetTickrateManager;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.setting.types.GSIntegerSetting;
import com.g4mesoft.ui.util.GSMathUtil;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameMode;

public class GSTpsModule implements GSIModule, GSISettingChangeListener, GSICarpetTickrateListener {

	public static final float DEFAULT_TPS = 20.0f;
	public static final float MIN_TPS = 0.01f;
	public static final float MAX_TPS = Float.MAX_VALUE;
	public static final float MS_PER_SEC = 1000.0f;

	private static final long SERVER_TPS_INTERVAL = 2000L;
	
	private static final float TPS_INCREMENT_INTERVAL = 1.0f;
	private static final float TONE_MULTIPLIER = (float)Math.pow(2.0, 1.0 / 12.0);
	
	public static final GSSettingCategory TPS_CATEGORY = new GSSettingCategory("tps");
	public static final GSSettingCategory BETTER_PISTONS_CATEGORY = new GSSettingCategory("betterPistons");

	public static final String KEY_CATEGORY = "tps";
	
	public static final int PISTON_ANIM_PAUSE_END = 0;
	public static final int PISTON_ANIM_PAUSE_MIDDLE = 1;
	public static final int PISTON_ANIM_PAUSE_BEGINNING = 2;
	public static final int PISTON_ANIM_NO_PAUSE = 3;
	
	public static final int AUTOMATIC_PISTON_RENDER_DISTANCE = -1;
	
	private static final int HOTKEY_MODE_DISABLED = 0;
	private static final int HOTKEY_MODE_CREATIVE = 1;
	private static final int HOTKEY_MODE_ALL      = 2;
	
	private static final int HOTKEY_FEEDBACK_DISABLED = 0;
	private static final int HOTKEY_FEEDBACK_STATUS   = 1;
	private static final int HOTKEY_FEEDBACK_CHAT     = 2;
	
	public static final int TPS_LABEL_DISABLED   = 0;
	public static final int TPS_LABEL_TOP_LEFT   = 1;
	public static final int TPS_LABEL_TOP_CENTER = 2;
	public static final int TPS_LABEL_TOP_RIGHT  = 3;
	
	public static final int PRETTY_SAND_DISABLED    = 0;
	public static final int PRETTY_SAND_PERFORMANCE = 1;
	public static final int PRETTY_SAND_FIDELITY    = 2;
	
	public static final DecimalFormat TPS_FORMAT = new DecimalFormat("0.0##", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private static final String TPS_CACHE_FILE_NAME = "tps_cache.txt";
	
	private float tps;
	private final List<GSITpsDependant> listeners;

	private int serverSyncTimer;
	private GSTpsMonitor serverTpsMonitor;
	private long lastServerTpsTime;
	
	private boolean fixedMovementOnDefaultTps = false;
	private float serverTps = Float.NaN;
	private final GSServerTickTimer serverTimer = new GSServerTickTimer(this);

	private GSIModuleManager manager;
	private GSCarpetCompat carpetCompat;

	public final GSBooleanSetting cShiftPitch;
	public final GSBooleanSetting cSyncTick;
	public final GSBooleanSetting cForceCarpetTickrate;
	public final GSIntegerSetting sSyncPacketInterval;
	public final GSIntegerSetting sTpsHotkeyMode;
	public final GSIntegerSetting sTpsHotkeyFeedback;
	public final GSBooleanSetting cNormalMovement;
	public final GSBooleanSetting cTweakerooFreecamHack;
	public final GSIntegerSetting cTpsLabel;
	public final GSBooleanSetting sBroadcastTps;
	public final GSBooleanSetting sRestoreTickrate;
	public final GSIntegerSetting sPrettySand;

	public final GSIntegerSetting cPistonAnimationType;
	public final GSBooleanSetting cCorrectPistonPushing;
	public final GSIntegerSetting cPistonRenderDistance;
	public final GSIntegerSetting sBlockEventDistance;
	public final GSBooleanSetting sParanoidMode;
	public final GSBooleanSetting sImmediateBlockBroadcast;
	
	public GSTpsModule() {
		tps = DEFAULT_TPS;
		listeners = new ArrayList<>();

		serverSyncTimer = 0;
		serverTpsMonitor = new GSTpsMonitor();
		lastServerTpsTime = Util.getMeasuringTimeMs();
		
		manager = null;
	
		cShiftPitch = new GSBooleanSetting("shiftPitch", true);
		cSyncTick = new GSBooleanSetting("syncTick", true);
		cForceCarpetTickrate = new GSBooleanSetting("forceCarpetTickrate", true);
		sSyncPacketInterval = new GSIntegerSetting("syncPacketInterval", 10, 1, 20);
		sTpsHotkeyMode = new GSIntegerSetting("hotkeyMode", HOTKEY_MODE_CREATIVE, 0, 2);
		sTpsHotkeyFeedback = new GSIntegerSetting("hotkeyFeedback", HOTKEY_FEEDBACK_STATUS, 0, 2);
		cNormalMovement = new GSBooleanSetting("normalMovement", true);
		cTweakerooFreecamHack = new GSBooleanSetting("tweakerooFreecamHack", true);
		cTpsLabel = new GSIntegerSetting("tpsLabel", TPS_LABEL_DISABLED, 0, 3);
		sBroadcastTps = new GSBooleanSetting("broadcastTps", true);
		sRestoreTickrate = new GSBooleanSetting("restoreTickrate", false);
		sPrettySand = new GSIntegerSetting("prettySand", PRETTY_SAND_PERFORMANCE, 0, 2);
		
		cPistonAnimationType = new GSIntegerSetting("pistonAnimationType", PISTON_ANIM_PAUSE_END, 0, 3);
		cCorrectPistonPushing = new GSBooleanSetting("correctPistonPushing", false);
		cPistonRenderDistance = new GSIntegerSetting("pistonRenderDistance", AUTOMATIC_PISTON_RENDER_DISTANCE, -1, 32);
		sBlockEventDistance = new GSIntegerSetting("blockEventDistance", 4, 0, 32);
		sParanoidMode = new GSBooleanSetting("paranoidMode", false);
		sImmediateBlockBroadcast = new GSBooleanSetting("immediateBlockBroadcast", false);
	}
	
	@Override
	public void init(GSIModuleManager manager) {
		this.manager = manager;
		
		carpetCompat = G4mespeedMod.getCarpetCompat();
		
		resetTps();
		serverTpsMonitor.reset();
		
		manager.runOnServer(managerServer -> {
			if (sRestoreTickrate.get()) {
				try {
					setTps(readTps(getTpsCacheFile()));
				} catch (IOException e) {
					G4mespeedMod.GS_LOGGER.warn("Unable to read tps from cache.");
				}
			}
			initCarpetTickrateManager(carpetCompat.getServerTickrateManager());
		});
		manager.runOnClient(managerClient -> {
			initCarpetTickrateManager(carpetCompat.getClientTickrateManager());
		});
	}

	@Override
	public void onClose() {
		clearTpsListeners();
		
		manager.runOnServer(serverManager -> {
			if (sRestoreTickrate.get()) {
				try {
					writeTps(tps, getTpsCacheFile());
				} catch (IOException e) {
					G4mespeedMod.GS_LOGGER.warn("Unable to write tps to cache.");
				}
			}
			closeCarpetTickrateManager(carpetCompat.getServerTickrateManager());
		});
		manager.runOnClient(managerClient -> {
			closeCarpetTickrateManager(carpetCompat.getClientTickrateManager());
		});
		
		manager = null;
	}
	
	@Override
	public void registerClientSettings(GSSettingManager settings) {
		settings.registerSettings(TPS_CATEGORY,
			cShiftPitch,
			cSyncTick,
			G4mespeedMod.getCarpetCompat().getClientTickrateManager().isTickrateLinked() ? cForceCarpetTickrate : null,
			cNormalMovement,
			G4mespeedMod.getTweakerooCompat().isCameraEntityRetreived() ? cTweakerooFreecamHack : null,
			cTpsLabel
		);
		// Tweakeroo hack is only enabled for normal movement setting.
		cTweakerooFreecamHack.setEnabledInGui(cNormalMovement.get());

		settings.registerSettings(BETTER_PISTONS_CATEGORY,
			cPistonAnimationType,
			cCorrectPistonPushing,
			cPistonRenderDistance
		);
		
		settings.addChangeListener(this);
	}

	@Override
	public void registerHotkeys(GSKeyManager keyManager) {
		keyManager.registerKey("reset", KEY_CATEGORY, GLFW.GLFW_KEY_M, 
				GSETpsHotkeyType.RESET_TPS, this::onClientHotkey, GSEKeyEventType.PRESS);
		
		keyManager.registerKey("increment", KEY_CATEGORY, GLFW.GLFW_KEY_PERIOD, 
				GSETpsHotkeyType.INCREMENT_TPS, this::onClientHotkey, GSEKeyEventType.PRESS);
		
		keyManager.registerKey("decrement", KEY_CATEGORY, GLFW.GLFW_KEY_COMMA, 
				GSETpsHotkeyType.DECREMENT_TPS, this::onClientHotkey, GSEKeyEventType.PRESS);
		
		keyManager.registerKey("double", KEY_CATEGORY, GLFW.GLFW_KEY_K, 
				GSETpsHotkeyType.DOUBLE_TPS, this::onClientHotkey, GSEKeyEventType.PRESS);

		keyManager.registerKey("halve", KEY_CATEGORY, GLFW.GLFW_KEY_J, 
				GSETpsHotkeyType.HALVE_TPS, this::onClientHotkey, GSEKeyEventType.PRESS);
	}
	
	@Override
	public void registerGlobalServerSettings(GSSettingManager settings) {
		settings.registerSettings(TPS_CATEGORY, 
			sSyncPacketInterval,
			sBroadcastTps,
			sTpsHotkeyMode,
			sTpsHotkeyFeedback,
			sRestoreTickrate,
			sPrettySand
		);
		settings.registerSettings(BETTER_PISTONS_CATEGORY,
			sBlockEventDistance,
			sParanoidMode,
			sImmediateBlockBroadcast
		);
	}
	
	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		GSTpsCommand.registerCommand(dispatcher);
	}
	
	@Override
	public void tick(boolean paused) {
		manager.runOnServer(managerServer -> {
			if (!paused) {
				serverSyncTimer++;
				
				int syncInterval = sSyncPacketInterval.get();
				if (serverSyncTimer >= syncInterval) {
					managerServer.sendPacketToAll(new GSServerSyncPacket(syncInterval));
					serverSyncTimer = 0;
				}
			}
			
			serverTpsMonitor.update(1);
			
			if (sBroadcastTps.get()) {
				long now = Util.getMeasuringTimeMs();
				
				// Note that the interval may be less than zero in case of the
				// first tick or in case of overflow / underflow.
				long serverTpsInterval = now - lastServerTpsTime;
				if (serverTpsInterval < 0L || serverTpsInterval > SERVER_TPS_INTERVAL) {
					float averageTps = serverTpsMonitor.getAverageTps();
					managerServer.sendPacketToAll(new GSServerTpsPacket(averageTps));
					lastServerTpsTime = now;
				}
			}
			pollCarpetTickrate(carpetCompat.getServerTickrateManager());
		});
		manager.runOnClient(managerClient -> {
			pollCarpetTickrate(carpetCompat.getClientTickrateManager());
		});
	}
	
	@Override
	public void onJoinG4mespeedServer(GSExtensionInfo coreInfo) {
		sendFixedMovementPacket();
	}
	
	public void onServerTps(float serverTps) {
		this.serverTps = serverTps;
		lastServerTpsTime = Util.getMeasuringTimeMs();
	}
	
	private void onClientHotkey(GSETpsHotkeyType hotkeyType) {
		manager.runOnClient(new Consumer<GSIClientModuleManager>() {
			
			@Override
			@Environment(EnvType.CLIENT)
			public void accept(GSIClientModuleManager managerClient) {
				MinecraftClient client = MinecraftClient.getInstance();
				boolean sneaking = client.options.keySneak.isPressed();
				
				if (managerClient.isG4mespeedServer()) {
					if (sTpsHotkeyMode.get() != HOTKEY_MODE_DISABLED) {
						// Only send the hotkey packet when the server
						// allows us to use hotkey controls.
						managerClient.sendPacket(new GSTpsHotkeyPacket(hotkeyType, sneaking));
					}
				} else if (client.interactionManager != null) { 
					if (isGameModeAllowingHotkeys(client.interactionManager.getCurrentGameMode())) {
						performHotkeyAction(hotkeyType, sneaking);
						
						if (client.inGameHud != null) {
							String formattedTps = TPS_FORMAT.format(tps);
							Text overlay = new TranslatableText("play.info.clientTpsChanged", formattedTps);
							client.inGameHud.setOverlayMessage(overlay, false);
						}
					} else if (client.inGameHud != null) {
						client.inGameHud.setOverlayMessage(new TranslatableText("play.info.hotkeysDisallowed"), false);
					}
				}
			}
		});
	}
	
	public void onPlayerHotkey(ServerPlayerEntity player, GSETpsHotkeyType type, boolean sneaking) {
		if (sTpsHotkeyMode.get() != HOTKEY_MODE_DISABLED && isPlayerAllowedTpsChange(player)) {
			if (isGameModeAllowingHotkeys(player.interactionManager.getGameMode())) {
				float oldTps = tps;
				performHotkeyAction(type, sneaking);
				
				if (!GSMathUtil.equalsApproximate(oldTps, tps)) {
					// Assume that the player changed the tps successfully.
					manager.runOnServer((serverManager) -> {
						Text name = player.getDisplayName();
						String formattedTps = TPS_FORMAT.format(tps);
						Text feedbackText = new TranslatableText("play.info.tpsChanged", name, formattedTps);
						
						for (ServerPlayerEntity otherPlayer : serverManager.getAllPlayers()) {
							if (isPlayerAllowedTpsChange(otherPlayer))
								sendHotkeyFeedback(otherPlayer, feedbackText);
						}
					});
				}
			} else {
				sendHotkeyFeedback(player, new TranslatableText("play.info.hotkeysDisallowed"));
			}
		}
	}
	
	private void sendHotkeyFeedback(ServerPlayerEntity player, Text feedbackText) {
		switch (sTpsHotkeyFeedback.get()) {
		case HOTKEY_FEEDBACK_DISABLED:
			break;
		case HOTKEY_FEEDBACK_STATUS:
			player.sendMessage(feedbackText, true);
			break;
		case HOTKEY_FEEDBACK_CHAT:
			player.sendMessage(feedbackText, false);
			break;
		default:
			break;
		}
	}
	
	public void performHotkeyAction(GSETpsHotkeyType type, boolean sneaking) {
		if (type == GSETpsHotkeyType.RESET_TPS) {
			resetTps();
			return;
		}
		
		// Easter egg for changing tps. Instead of changing tps 
		// normally we are going to change it one note at a time.
		// For this we'll need some music theory in relation to
		// the physics of sound:

		// We all know sound is simply waves that have a specific 
		// frequency. In other words we can express every tone in
		// music as a given frequency for that tone. There are 12
		// fundamental tones in each octave - and infinitely many
		// octaves. The important thing here is that to transform
		// a signal a single octave up we can simply multiply the
		// frequency by two. In other words it's simple to switch
		// between octaves. Tones on the other hand is slightly
		// different. Here we need some more theory. To step from
		// one note a constant amount of notes up, we can use the
		// following formula:
		//     f_n' = f_n * c, where f_n and f_n' are frequencies
		//                     and c is constant for that amount.

		// This also means that if c is the constant that steps one
		// note up then we can step from one octave to the next by
		// doing f_n' = f_n * c^12. In other words we now know what
		// the constant c should be for stepping up a single note,
		// 'cause we could also step up one octave by multiplying by
		// two! This means that c^12 = 2 <=> c = 2^(1/12).
		
		// Now let's apply this theory!
		
		switch (type) {
		case INCREMENT_TPS:
			if (sneaking) {
				// Since we're applying the pitch of sound in relation
				// to targetPitch = pitch * tps / DEFAULT_TPS, we can
				// simply multiply the tps by this amount to achieve
				// incrementing the tone by one!
				setTps(tps * TONE_MULTIPLIER);
			} else {
				setTps(tps + TPS_INCREMENT_INTERVAL);
			}
			break;
		case DECREMENT_TPS:
			if (sneaking) {
				// Decrementing one tone is just division by the constant.
				setTps(tps / TONE_MULTIPLIER);
			} else {
				setTps(tps - TPS_INCREMENT_INTERVAL);
			}
			break;

		case DOUBLE_TPS:
			setTps(tps * 2.0f);
			break;
		case HALVE_TPS:
			setTps(tps / 2.0f);
			break;
			
		default:
			return;
		}
	}

	@Override
	public void onDisconnectServer() {
		resetTps();
		
		serverTps = Float.NaN;
	}

	@Override
	public void onG4mespeedClientJoin(ServerPlayerEntity player, GSExtensionInfo coreInfo) {
		manager.runOnServer(managerServer -> managerServer.sendPacket(new GSTpsChangePacket(tps), player));
	}

	public void addTpsListener(GSITpsDependant listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null");
		synchronized(listeners) {
			listeners.add(listener);
			listener.tpsChanged(tps, 0.0f);
		}
	}

	public void removeTpsListener(GSITpsDependant listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	private void clearTpsListeners() {
		synchronized(listeners) {
			listeners.clear();
		}
	}
	
	public void resetTps() {
		setTps(DEFAULT_TPS);
	}
	
	public void setTps(float tps) {
		tps = GSMathUtil.clamp(tps, MIN_TPS, MAX_TPS);
		
		if (!GSMathUtil.equalsApproximate(tps, this.tps)) {
			float oldTps = this.tps;
			this.tps = tps;
			
			synchronized(listeners) {
				for (GSITpsDependant listener : listeners)
					listener.tpsChanged(tps, oldTps);
			}
			
			manager.runOnServer(managerServer -> { 
				managerServer.sendPacketToAll(new GSTpsChangePacket(this.tps));
				
				// Setup sync timer so it will send sync in the 
				// next tick (this ensures that the client had
				// time to react to the previous packet).
				serverSyncTimer = sSyncPacketInterval.get();

				// Reset the tps monitor. This should only happen
				// on the server, since it would otherwise create
				// a de-sync with the server tick cycle.
				serverTpsMonitor.reset();

				lastServerTpsTime = Util.getMeasuringTimeMs();
				carpetCompat.getServerTickrateManager().setTickrate(this.tps);
			});
			manager.runOnClient(managerClient -> {
				carpetCompat.getClientTickrateManager().setTickrate(this.tps);
			});
		}
	}
	
	public boolean isGameModeAllowingHotkeys(GameMode gameMode) {
		switch (sTpsHotkeyMode.get()) {
		case HOTKEY_MODE_CREATIVE:
			return (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR);
		case HOTKEY_MODE_ALL:
			return true;
		case HOTKEY_MODE_DISABLED:
		default:
			return false;
		}
	}

	public boolean isPlayerAllowedTpsChange(PlayerEntity player) {
		return player.hasPermissionLevel(GSServerController.OP_PERMISSION_LEVEL);
	}
	
	@Override
	public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		if (setting == cNormalMovement) {
			sendFixedMovementPacket();
			cTweakerooFreecamHack.setEnabledInGui(cNormalMovement.get());
		}
	}
	
	private void sendFixedMovementPacket() {
		manager.runOnClient(clientManager -> {
			clientManager.sendPacket(new GSPlayerFixedMovementPacket(cNormalMovement.get()));
		});
	}

	/* Carpet compatibility methods */
	
	public void initCarpetTickrateManager(GSICarpetTickrateManager tickrateManager) {
		tickrateManager.onInit(manager);
		if (tickrateManager.isTickrateLinked())
			tickrateManager.addListener(this);
	}

	public void closeCarpetTickrateManager(GSICarpetTickrateManager tickrateManager) {
		tickrateManager.removeListener(this);
		tickrateManager.onClose();
	}
	
	@Override
	public void carpetTickrateChanged(float tickrate) {
		setTps(tickrate);
	}
	
	private void pollCarpetTickrate(GSICarpetTickrateManager tickrateManager) {
		if (tickrateManager.isPollingCompatMode()) {
			// With older versions of carpet we have to poll the current tps
			// manually since we don't receive an event directly when it changes.
			float tickrate = tickrateManager.getTickrate();
			if (!GSMathUtil.equalsApproximate(tickrate, tps))
				setTps(tickrate);
		}
	}
	
	public float getMsPerTick() {
		return MS_PER_SEC / tps;
	}

	public float getTps() {
		return tps;
	}
	
	public boolean isDefaultTps() {
		return GSMathUtil.equalsApproximate(tps, DEFAULT_TPS);
	}

	private float readTps(File file) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
			String line;
			if ((line = br.readLine()) != null)
				return Float.parseFloat(line);

			throw new IOException("Tps file is empty");
		} catch (NumberFormatException e) {
			throw new IOException("Invalid tps format", e);
		}
	}

	private void writeTps(float tps, File file) throws IOException {
		try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
			bw.write(Float.toString(tps));
		}
	}
	
	private File getTpsCacheFile() {
		return new File(manager.getCacheFile(), TPS_CACHE_FILE_NAME);
	}
	
	@Environment(EnvType.CLIENT)
	public void onServerSyncPacket(int packetInterval) {
		serverTimer.onSyncPacket(packetInterval);
		
		// This is only for approximating the server tps
		serverTpsMonitor.update(packetInterval);
	}
	
	@Environment(EnvType.CLIENT)
	public float getServerTps() {
		if (sBroadcastTps.get() && Float.isFinite(serverTps))
			return serverTps;
		return serverTpsMonitor.getAverageTps();
	}

	@Environment(EnvType.CLIENT)
	public GSServerTickTimer getServerTimer() {
		return serverTimer;
	}
	
	@Environment(EnvType.CLIENT)
	public boolean isMainPlayerFixedMovement() {
		if (cNormalMovement.get() && (!isDefaultTps() || fixedMovementOnDefaultTps)) {
			PlayerEntity player = GSClientController.getInstance().getPlayer();

			// Do not enable fixed movement if player has a vehicle.
			if (player != null && !player.hasVehicle()) {
				// Carpet allows clients to have different tps than the server,
				// do not enable fixed movement if carpet is in this mode.
				if (carpetCompat.getClientTickrateManager().isTickrateLinked())
					return cForceCarpetTickrate.get();
				return true;
			}
		}
		
		return false;
	}

	@Environment(EnvType.CLIENT)
	public boolean isPlayerFixedMovement(AbstractClientPlayerEntity player) {
		// Only enable fixed movement if tps is different from default.
		if (!isDefaultTps() || fixedMovementOnDefaultTps) {
			GSClientController controller = GSClientController.getInstance();
		
			// Check if is is the main player.
			if (player == controller.getPlayer())
				return isMainPlayerFixedMovement();
		
			if (!controller.isG4mespeedServer())
				return GSMathUtil.equalsApproximate(getServerTps(), DEFAULT_TPS);
			return ((GSIAbstractClientPlayerEntityAccess)player).gs_isFixedMovement();
		}
		
		return false;
	}
	
	public boolean isFixedMovementOnDefaultTps() {
		return fixedMovementOnDefaultTps;
	}

	public void setFixedMovementOnDefaultTps(boolean fixedMovementOnDefaultTps) {
		this.fixedMovementOnDefaultTps = fixedMovementOnDefaultTps;
	}
	
	
	@Environment(EnvType.CLIENT)
	public void onClientGameModeChanged(GameMode gameMode) {
		GSClientController controller = GSClientController.getInstance();
		if (controller.isConnectedToServer() && !controller.isG4mespeedServer() && !isGameModeAllowingHotkeys(gameMode)) {
			// User is connected to a non-g4mespeed server, and changed to a game mode that
			// does not allow client tps changes. Ensure that the player can not cheat by
			// resetting to default tps here.
			setTps(DEFAULT_TPS);
		}
	}
}
