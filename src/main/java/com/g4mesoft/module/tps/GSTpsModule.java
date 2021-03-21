package com.g4mesoft.module.tps;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionInfo;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.client.GSIModuleManagerClient;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.core.compat.GSICarpetCompatTickrateListener;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.setting.types.GSIntegerSetting;
import com.g4mesoft.util.GSMathUtils;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public class GSTpsModule implements GSIModule, GSISettingChangeListener, GSICarpetCompatTickrateListener {

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
	public static final int PISTON_ANIM_PAUSE_BEGINNING = 1;
	public static final int PISTON_ANIM_NO_PAUSE = 2;
	
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
	
	public static final DecimalFormat TPS_FORMAT = new DecimalFormat("0.0##", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private float tps;
	private final List<GSITpsDependant> listeners;

	private int serverSyncTimer;
	private GSTpsMonitor serverTpsMonitor;
	private long lastServerTpsTime;

	@Environment(EnvType.CLIENT)
	private float serverTps = Float.NaN;
	@Environment(EnvType.CLIENT)
	private final GSServerTickTimer serverTimer = new GSServerTickTimer(this);

	private GSIModuleManager manager;

	public final GSBooleanSetting cShiftPitch;
	public final GSBooleanSetting cSyncTick;
	public final GSBooleanSetting cForceCarpetTickrate;
	public final GSIntegerSetting sSyncPacketInterval;
	public final GSIntegerSetting sTpsHotkeyMode;
	public final GSIntegerSetting sTpsHotkeyFeedback;
	public final GSBooleanSetting cNormalMovement;
	public final GSIntegerSetting cTpsLabel;
	public final GSBooleanSetting sBroadcastTps;

	public final GSIntegerSetting cPistonAnimationType;
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
		cNormalMovement = new GSBooleanSetting("normalMovement", false);
		cTpsLabel = new GSIntegerSetting("tpsLabel", TPS_LABEL_DISABLED, 0, 3);
		sBroadcastTps = new GSBooleanSetting("broadcastTps", true);
		
		cPistonAnimationType = new GSIntegerSetting("pistonAnimationType", PISTON_ANIM_PAUSE_END, 0, 2);
		cPistonRenderDistance = new GSIntegerSetting("pistonRenderDistance", AUTOMATIC_PISTON_RENDER_DISTANCE, -1, 32);
		sBlockEventDistance = new GSIntegerSetting("blockEventDistance", 4, 0, 32);
		sParanoidMode = new GSBooleanSetting("paranoidMode", false);
		sImmediateBlockBroadcast = new GSBooleanSetting("immediateBlockBroadcast", false);
	}
	
	@Override
	public void init(GSIModuleManager manager) {
		this.manager = manager;
		
		resetTps();
		serverTpsMonitor.reset();
		
		G4mespeedMod.getInstance().getCarpetCompat().addCarpetTickrateListener(this);
	}

	@Override
	public void onClose() {
		clearTpsListeners();
		
		G4mespeedMod.getInstance().getCarpetCompat().removeCarpetTickrateListener(this);

		manager = null;
	}
	
	@Override
	public void registerClientSettings(GSSettingManager settings) {
		settings.registerSetting(TPS_CATEGORY, cShiftPitch);
		settings.registerSetting(TPS_CATEGORY, cSyncTick);
		if (G4mespeedMod.getInstance().getCarpetCompat().isTickrateLinked())
			settings.registerSetting(TPS_CATEGORY, cForceCarpetTickrate);
		settings.registerSetting(TPS_CATEGORY, cNormalMovement);
		settings.registerSetting(TPS_CATEGORY, cTpsLabel);

		settings.registerSetting(BETTER_PISTONS_CATEGORY, cPistonAnimationType);
		settings.registerSetting(BETTER_PISTONS_CATEGORY, cPistonRenderDistance);
		
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
	public void registerServerSettings(GSSettingManager settings) {
		settings.registerSetting(TPS_CATEGORY, sSyncPacketInterval);
		settings.registerSetting(TPS_CATEGORY, sBroadcastTps);
		settings.registerSetting(TPS_CATEGORY, sTpsHotkeyMode);
		settings.registerSetting(TPS_CATEGORY, sTpsHotkeyFeedback);

		settings.registerSetting(BETTER_PISTONS_CATEGORY, sBlockEventDistance);
		settings.registerSetting(BETTER_PISTONS_CATEGORY, sParanoidMode);
		settings.registerSetting(BETTER_PISTONS_CATEGORY, sImmediateBlockBroadcast);
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
				
				int syncInterval = sSyncPacketInterval.getValue();
				if (serverSyncTimer >= syncInterval) {
					managerServer.sendPacketToAll(new GSServerSyncPacket(syncInterval));
					serverSyncTimer = 0;
				}
			}
			
			serverTpsMonitor.update(1);
			
			if (sBroadcastTps.getValue()) {
				long now = Util.getMeasuringTimeMs();
				
				// Note that the interval may be less than zero in case of the
				// first tick or in case of overflow / underflow.
				long sererTpsInterval = now - lastServerTpsTime;
				if (sererTpsInterval < 0L || sererTpsInterval > SERVER_TPS_INTERVAL) {
					float averageTps = serverTpsMonitor.getAverageTps();
					managerServer.sendPacketToAll(new GSServerTpsPacket(averageTps));
					lastServerTpsTime = now;
				}
			}
		});

		GSCarpetCompat carpetCompat = G4mespeedMod.getInstance().getCarpetCompat();
		if (carpetCompat.isCarpetDetected() && carpetCompat.isOutdatedCompatMode()) {
			// With older versions of carpet we have to poll the current tps
			// manually since we don't receive an event directly when it changes.
			float carpetTickrate = carpetCompat.getCarpetTickrate();
			if (!GSMathUtils.equalsApproximate(carpetTickrate, tps))
				setTps(carpetTickrate);
		}
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
		manager.runOnClient(new Consumer<GSIModuleManagerClient>() {
			
			@Override
			@Environment(EnvType.CLIENT)
			public void accept(GSIModuleManagerClient managerClient) {
				MinecraftClient client = MinecraftClient.getInstance();
				boolean sneaking = client.options.keySneak.isPressed();
				
				if (managerClient.isG4mespeedServer()) {
					if (sTpsHotkeyMode.getValue() != HOTKEY_MODE_DISABLED) {
						// Only send the hotkey packet when the server
						// allows us to use hotkey controls.
						managerClient.sendPacket(new GSTpsHotkeyPacket(hotkeyType, sneaking));
					}
				} else if (client.player != null) { 
					if (isGameModeAllowingHotkeys(client.player)) {
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
		if (sTpsHotkeyMode.getValue() != HOTKEY_MODE_DISABLED && isPlayerAllowedTpsChange(player)) {
			if (isGameModeAllowingHotkeys(player)) {
				float oldTps = tps;
				performHotkeyAction(type, sneaking);
				
				if (!GSMathUtils.equalsApproximate(oldTps, tps)) {
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
		switch (sTpsHotkeyFeedback.getValue()) {
		case HOTKEY_FEEDBACK_DISABLED:
			break;
		case HOTKEY_FEEDBACK_STATUS:
			player.addChatMessage(feedbackText, true);
			break;
		case HOTKEY_FEEDBACK_CHAT:
			player.addChatMessage(feedbackText, false);
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
		tps = GSMathUtils.clamp(tps, MIN_TPS, MAX_TPS);
		
		if (!GSMathUtils.equalsApproximate(tps, this.tps)) {
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
				serverSyncTimer = sSyncPacketInterval.getValue();

				// Reset the tps monitor. This should only happen
				// on the server, since it would otherwise create
				// a de-sync with the server tick cycle.
				serverTpsMonitor.reset();

				lastServerTpsTime = Util.getMeasuringTimeMs();
			});
			
			GSCarpetCompat carpetCompat = G4mespeedMod.getInstance().getCarpetCompat();
			if (carpetCompat.isCarpetDetected() && carpetCompat.isTickrateLinked())
				carpetCompat.notifyTickrateChange(tps);
		}
	}
	
	public boolean isGameModeAllowingHotkeys(PlayerEntity player) {
		switch (sTpsHotkeyMode.getValue()) {
		case HOTKEY_MODE_DISABLED:
			return false;
		case HOTKEY_MODE_CREATIVE:
			return (player.isCreative() || player.isSpectator());
		case HOTKEY_MODE_ALL:
			return true;
		}
		
		return false;
	}
	
	public boolean isPlayerAllowedTpsChange(PlayerEntity player) {
		return player.allowsPermissionLevel(GSControllerServer.OP_PERMISSION_LEVEL);
	}
	
	@Override
	public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		if (setting == cNormalMovement)
			sendFixedMovementPacket();
	}
	
	private void sendFixedMovementPacket() {
		manager.runOnClient(clientManager -> {
			clientManager.sendPacket(new GSPlayerFixedMovementPacket(cNormalMovement.getValue()));
		});
	}

	@Override
	public void onCarpetTickrateChanged(float tickrate) {
		setTps(tickrate);
	}
	
	public float getMsPerTick() {
		return MS_PER_SEC / tps;
	}

	public float getTps() {
		return tps;
	}

	@Environment(EnvType.CLIENT)
	public void onServerSyncPacket(int packetInterval) {
		serverTimer.onSyncPacket(packetInterval);
		
		// This is only for approximating the server tps
		serverTpsMonitor.update(packetInterval);
	}
	
	@Environment(EnvType.CLIENT)
	public float getServerTps() {
		if (sBroadcastTps.getValue() && Float.isFinite(serverTps))
			return serverTps;
		return serverTpsMonitor.getAverageTps();
	}

	@Environment(EnvType.CLIENT)
	public GSServerTickTimer getServerTimer() {
		return serverTimer;
	}
	
	@Environment(EnvType.CLIENT)
	public boolean shouldCorrectMovement() {
		if (cNormalMovement.getValue() && !GSMathUtils.equalsApproximate(tps, DEFAULT_TPS)) {
			PlayerEntity player = GSControllerClient.getInstance().getPlayer();

			if (player != null && !player.hasVehicle()) {
				if (G4mespeedMod.getInstance().getCarpetCompat().isTickrateLinked())
					return cForceCarpetTickrate.getValue();
				return true;
			}
		}
		
		return false;
	}
}
