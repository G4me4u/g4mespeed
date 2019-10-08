package com.g4mesoft.module.tps;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.setting.GSClientSettings;
import com.g4mesoft.setting.GSIKeyBinding;
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
	
	public static final float TPS_INCREMENT_INTERVAL = 1.0f;
	public static final float SLOW_INTERVAL = 0.5f;
	
	public static final int SERVER_SYNC_INTERVAL = 10;
	
	public static final int TPS_INTRODUCTION_VERSION = 100;
	
	public static final GSSettingCategory SETTING_CATEGORY = new GSSettingCategory("tps");
	
	public static final int PISTON_ANIM_PAUSE_BEGINNING = 0;
	public static final int PISTON_ANIM_NO_PAUSE = 1;
	public static final int PISTON_ANIM_PAUSE_END = 2;
	
	private float tps;
	private final List<GSITpsDependant> listeners;

	private boolean sneaking;
	private int serverSyncTimer;

	private GSIModuleManager manager;

	public final GSBooleanSetting cShiftPitch;
	public final GSBooleanSetting cCullMovingBlocks;
	public final GSIntegerSetting cPistonAnimationType;
	
	public final GSBooleanSetting cSyncTick;
	public final GSFloatSetting cSyncTickAggression;
	
	public GSTpsModule() {
		tps = DEFAULT_TPS;
		listeners = new ArrayList<GSITpsDependant>();

		sneaking = false;
		serverSyncTimer = 0;
		
		manager = null;
	
		cShiftPitch = new GSBooleanSetting("shiftPitch", true);
		cCullMovingBlocks = new GSBooleanSetting("cullMovingBlocks", true);
		cPistonAnimationType = new GSIntegerSetting("pistonAnimationType", 0, 0, 2);
	
		cSyncTick = new GSBooleanSetting("syncTick", true);
		cSyncTickAggression = new GSFloatSetting("syncTickAggression", 0.05f, 0.0f, 1.0f, 0.05f);
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
				
				// Setup sync timer to it will send sync in the 
				// next tick (this ensures that the client had
				// time to react to the previous packet).
				serverSyncTimer = SERVER_SYNC_INTERVAL;
			});
		}
	}

	public void performHotkeyAction(GSETpsHotkeyType type, boolean sneaking) {
		if (type == GSETpsHotkeyType.RESET_TPS) {
			resetTps();
			return;
		}
		
		float newTps;
		
		switch (type) {
		case INCREMENT_TPS:
			newTps = tps + TPS_INCREMENT_INTERVAL;
			break;
		case DECREMENT_TPS:
			newTps = tps - TPS_INCREMENT_INTERVAL;
			break;
			
		case DOUBLE_TPS:
			newTps = tps * 2.0f;
			break;
		case HALF_TPS:
			newTps = tps / 2.0f;
			break;

		default:
			return;
		}
		
		if (sneaking)
			newTps += (tps - newTps) * 0.5f;
	
		setTps(newTps);
	}
	
	@Override
	public void init(GSIModuleManager manager) {
		this.manager = manager;
		
		manager.runOnClient((managerClient) -> {
			GSSettingManager settings = manager.getSettingManager();
			settings.registerSetting(SETTING_CATEGORY, cShiftPitch);
			settings.registerSetting(SETTING_CATEGORY, cCullMovingBlocks);
			settings.registerSetting(SETTING_CATEGORY, cPistonAnimationType);
			settings.registerSetting(SETTING_CATEGORY, cSyncTick);
			settings.registerSetting(SETTING_CATEGORY, cSyncTickAggression);
		});
	}
	
	@Override
	public void tick() {
		manager.runOnServer(managerServer -> {
			serverSyncTimer++;
			
			if (serverSyncTimer >= SERVER_SYNC_INTERVAL) {
				managerServer.sendPacketToAll(new GSServerSyncPacket());
				serverSyncTimer = 0;
			}
		});
	}

	@Override
	public void keyReleased(int key, int scancode, int mods) {
		if (((GSIKeyBinding)MinecraftClient.getInstance().options.keySneak).getKeyCode() == key)
			sneaking = false;
	}

	@Override
	public void keyPressed(int key, int scancode, int mods) {
		if (((GSIKeyBinding)MinecraftClient.getInstance().options.keySneak).getKeyCode() == key) {
			sneaking = true;
			return;
		}
		
		GSClientSettings settings = GSControllerClient.getInstance().getClientSettings();

		GSETpsHotkeyType hotkeyType;
		if (((GSIKeyBinding)settings.gsResetTpsKey).getKeyCode() == key) {
			hotkeyType = GSETpsHotkeyType.RESET_TPS;
		} else if (((GSIKeyBinding)settings.gsIncreaseTpsKey).getKeyCode() == key) {
			hotkeyType = GSETpsHotkeyType.INCREMENT_TPS;
		} else if (((GSIKeyBinding)settings.gsDecreaseTpsKey).getKeyCode() == key) {
			hotkeyType = GSETpsHotkeyType.DECREMENT_TPS;
		} else if (((GSIKeyBinding)settings.gsDoubleTpsKey).getKeyCode() == key) {
			hotkeyType = GSETpsHotkeyType.DOUBLE_TPS;
		} else if (((GSIKeyBinding)settings.gsHalfTpsKey).getKeyCode() == key) {
			hotkeyType = GSETpsHotkeyType.HALF_TPS;
		} else {
			return;
		}
		
		manager.runOnClient(managerClient -> {
			if (managerClient.getServerVersion() >= TPS_INTRODUCTION_VERSION) {
				managerClient.sendPacket(new GSTpsHotkeyPacket(hotkeyType, sneaking));
			} else {
				performHotkeyAction(hotkeyType, sneaking);
				
				if (MinecraftClient.getInstance().inGameHud != null) {
					Text msg = new TranslatableText("Changed tps on client: %s", tps);
					MinecraftClient.getInstance().inGameHud.addChatMessage(MessageType.GAME_INFO, msg);
				}
			}
		});
	}

	@Override
	public void onDisconnectServer() {
		resetTps();
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		GSTpsCommand.registerCommand(dispatcher);
	}

	@Override
	public void onG4mespeedClientJoin(ServerPlayerEntity player, int version) {
		manager.runOnServer(managerServer -> managerServer.sendPacket(new GSTpsChangePacket(tps), player));
	}

	@Override
	public void onServerShutdown() {
		resetTps();
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
