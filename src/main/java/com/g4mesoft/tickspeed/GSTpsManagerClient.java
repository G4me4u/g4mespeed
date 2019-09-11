package com.g4mesoft.tickspeed;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSControllerClient;
import com.g4mesoft.settings.GSIKeyBinding;
import com.g4mesoft.settings.GSSettings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSTpsManagerClient extends GSTpsManager {

	private final MinecraftClient minecraft;
	
	private float incInterval;
	
	public GSTpsManagerClient(GSControllerClient controller) {
		super(controller);

		minecraft = MinecraftClient.getInstance();
	
		incInterval = SLOW_INTERVAL;
	}

	private void requestServerTpsReset() {
		if (isServerTpsSupported()) {
			((GSControllerClient)controller).sendPacket(new GSTpsResetPacket());
		} else {
			resetTps();
		}
	}

	private void requestServerTpsChange(float requestedTps) {
		if (isServerTpsSupported()) {
			((GSControllerClient)controller).sendPacket(new GSTpsChangePacket(requestedTps));
		} else {
			setTps(requestedTps);
		}
	}
	
	public void keyReleased(int key, int scancode, int mods) {
		if (key == ((GSIKeyBinding)minecraft.options.keySneak).getKeyCode())
			incInterval = FAST_INTERVAL;
	}

	public void keyPressed(int key, int scancode, int mods) {
		GSSettings settings = G4mespeedMod.getInstance().getSettings();
		
		if (((GSIKeyBinding)settings.gsResetTpsKey).getKeyCode() == key) {
			requestServerTpsReset();
		} else if (((GSIKeyBinding)minecraft.options.keySneak).getKeyCode() == key) {
			incInterval = SLOW_INTERVAL;
		} else if (((GSIKeyBinding)settings.gsIncreaseTpsKey).getKeyCode() == key) {
			requestServerTpsChange(tps + incInterval);
		} else if (((GSIKeyBinding)settings.gsDecreaseTpsKey).getKeyCode() == key) {
			requestServerTpsChange(tps - incInterval);
		} else if (((GSIKeyBinding)settings.gsHalfTpsKey).getKeyCode() == key) {
			requestServerTpsChange(tps * 0.5f);
		} else if (((GSIKeyBinding)settings.gsDoubleTpsKey).getKeyCode() == key) {
			requestServerTpsChange(tps * 2.0f);
		}
	}

	public void keyRepeated(int key, int scancode, int mods) {
	}
	
	@Override
	public boolean setTps(float tps) {
		if (super.setTps(tps)) {
			if (!isServerTpsSupported() && minecraft.inGameHud != null) {
				Text msg = new TranslatableText("Changed tps on client: %s", this.tps);
				minecraft.inGameHud.addChatMessage(MessageType.GAME_INFO, msg);
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean isServerTpsSupported() {
		return ((GSControllerClient)controller).getServerVersion() >= TPS_INTRODUCTION_VERSION;
	}
}
