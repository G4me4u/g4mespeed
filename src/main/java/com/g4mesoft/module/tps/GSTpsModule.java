package com.g4mesoft.module.tps;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;
import com.g4mesoft.setting.GSSettingCategory;
import com.g4mesoft.setting.GSSettingManager;
import com.g4mesoft.setting.types.GSBooleanSetting;
import com.g4mesoft.setting.types.GSFloatSetting;
import com.g4mesoft.setting.types.GSIntegerSetting;
import com.g4mesoft.util.GSMathUtils;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSTpsModule implements GSIModule {

	public static final float DEFAULT_TPS = 20.0f;
	public static final float MIN_TPS = 0.01f;
	public static final float MAX_TPS = Float.MAX_VALUE;
	public static final float MS_PER_SEC = 1000.0f;
	
	private static final float TPS_INCREMENT_INTERVAL = 1.0f;
	private static final float TONE_MULTIPLIER = (float)Math.pow(2.0, 1.0 / 12.0);
	
	public static final GSVersion TPS_INTRODUCTION_VERSION = new GSVersion(1, 0, 0);
	
	public static final GSSettingCategory TPS_CATEGORY = new GSSettingCategory("tps");
	public static final GSSettingCategory BETTER_PISTONS_CATEGORY = new GSSettingCategory("betterPistons");

	public static final String KEY_CATEGORY = "tps";
	
	public static final int PISTON_ANIM_PAUSE_BEGINNING = 0;
	public static final int PISTON_ANIM_NO_PAUSE = 1;
	public static final int PISTON_ANIM_PAUSE_END = 2;
	
	public static final int AUTOMATIC_PISTON_RENDER_DISTANCE = 0;
	
	private float tps;
	private final List<GSITpsDependant> listeners;

	private int serverSyncTimer;

	private GSIModuleManager manager;

	public final GSBooleanSetting cShiftPitch;
	public final GSBooleanSetting cSyncTick;
	public final GSFloatSetting cSyncTickAggression;
	public final GSIntegerSetting sSyncPacketInterval;

	public final GSBooleanSetting cCullMovingBlocks;
	public final GSIntegerSetting cPistonAnimationType;
	public final GSIntegerSetting cPistonRenderDistance;
	public final GSIntegerSetting sBlockEventDistance;
	
	public GSTpsModule() {
		tps = DEFAULT_TPS;
		listeners = new ArrayList<GSITpsDependant>();

		serverSyncTimer = 0;
		
		manager = null;
	
		cShiftPitch = new GSBooleanSetting("shiftPitch", true);
		cSyncTick = new GSBooleanSetting("syncTick", true);
		cSyncTickAggression = new GSFloatSetting("syncTickAggression", 0.05f, 0.0f, 1.0f, 0.05f);
		sSyncPacketInterval = new GSIntegerSetting("syncPacketInterval", 10, 1, 20);

		cCullMovingBlocks = new GSBooleanSetting("cullMovingBlocks", true);
		cPistonAnimationType = new GSIntegerSetting("pistonAnimationType", 0, 0, 2);
		cPistonRenderDistance = new GSIntegerSetting("pistonRenderDistance", AUTOMATIC_PISTON_RENDER_DISTANCE, 0, 32);
		sBlockEventDistance = new GSIntegerSetting("blockEventDistance", 4, 0, 32);
	}
	
	@Override
	public void init(GSIModuleManager manager) {
		this.manager = manager;
	}

	@Override
	public void onClose() {
		resetTps();
		
		clearTpsListeners();

		manager = null;
	}
	
	@Override
	public void registerClientSettings(GSSettingManager settings) {
		settings.registerSetting(TPS_CATEGORY, cShiftPitch);
		settings.registerSetting(TPS_CATEGORY, cSyncTick);
		settings.registerSetting(TPS_CATEGORY, cSyncTickAggression);
		
		settings.registerSetting(BETTER_PISTONS_CATEGORY, cCullMovingBlocks);
		settings.registerSetting(BETTER_PISTONS_CATEGORY, cPistonAnimationType);
		settings.registerSetting(BETTER_PISTONS_CATEGORY, cPistonRenderDistance);
	}

	@Override
	public void registerHotkeys(GSKeyManager keyManager) {
		keyManager.registerKey("reset", KEY_CATEGORY, GLFW.GLFW_KEY_M, 
				GSETpsHotkeyType.RESET_TPS, this::onHotkey, GSEKeyEventType.PRESS);
		
		keyManager.registerKey("increment", KEY_CATEGORY, GLFW.GLFW_KEY_PERIOD, 
				GSETpsHotkeyType.INCREMENT_TPS, this::onHotkey, GSEKeyEventType.PRESS);
		
		keyManager.registerKey("decrement", KEY_CATEGORY, GLFW.GLFW_KEY_COMMA, 
				GSETpsHotkeyType.DECREMENT_TPS, this::onHotkey, GSEKeyEventType.PRESS);
		
		keyManager.registerKey("double", KEY_CATEGORY, GLFW.GLFW_KEY_K, 
				GSETpsHotkeyType.DOUBLE_TPS, this::onHotkey, GSEKeyEventType.PRESS);

		keyManager.registerKey("half", KEY_CATEGORY, GLFW.GLFW_KEY_J, 
				GSETpsHotkeyType.HALF_TPS, this::onHotkey, GSEKeyEventType.PRESS);
	}
	
	@Override
	public void registerServerSettings(GSSettingManager settings) {
		settings.registerSetting(TPS_CATEGORY, sSyncPacketInterval);
		settings.registerSetting(BETTER_PISTONS_CATEGORY, sBlockEventDistance);
	}
	
	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		GSTpsCommand.registerCommand(dispatcher);
	}
	
	@Override
	public void tick() {
		manager.runOnServer(managerServer -> {
			serverSyncTimer++;
			
			int syncInterval = sSyncPacketInterval.getValue();
			if (serverSyncTimer >= syncInterval) {
				managerServer.sendPacketToAll(new GSServerSyncPacket(syncInterval), TPS_INTRODUCTION_VERSION);
				serverSyncTimer = 0;
			}
		});
	}
	
	private void onHotkey(GSETpsHotkeyType hotkeyType) {
		manager.runOnClient(managerClient -> {
			MinecraftClient client = MinecraftClient.getInstance();
			boolean sneaking = client.options.keySneak.isPressed();

			if (managerClient.getServerVersion().isGreaterThanOrEqualTo(TPS_INTRODUCTION_VERSION)) {
				managerClient.sendPacket(new GSTpsHotkeyPacket(hotkeyType, sneaking));
			} else {
				performHotkeyAction(hotkeyType, sneaking);
				
				if (client.inGameHud != null) {
					Text msg = new TranslatableText("command.tps.clientOnly", tps);
					client.inGameHud.addChatMessage(MessageType.GAME_INFO, msg);
				}
			}
		});
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
		case HALF_TPS:
			setTps(tps / 2.0f);
			break;
			
		default:
			return;
		}
	}

	@Override
	public void onDisconnectServer() {
		resetTps();
	}

	@Override
	public void onG4mespeedClientJoin(ServerPlayerEntity player, GSVersion version) {
		if (version.isGreaterThanOrEqualTo(TPS_INTRODUCTION_VERSION))
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
			});
		}
	}
	
	public boolean isPlayerAllowedTpsChange(ServerPlayerEntity player) {
		return player.allowsPermissionLevel(GSControllerServer.OP_PERMISSION_LEVEL);
	}
	
	public float getMsPerTick() {
		return MS_PER_SEC / tps;
	}

	public float getTps() {
		return tps;
	}
}
