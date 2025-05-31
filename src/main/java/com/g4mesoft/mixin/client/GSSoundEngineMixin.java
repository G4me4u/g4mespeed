package com.g4mesoft.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

import net.minecraft.client.sound.instance.SoundInstance;
import net.minecraft.client.sound.system.SoundEngine;

@Mixin(SoundEngine.class)
public abstract class GSSoundEngineMixin implements GSITpsDependant, GSISettingChangeListener {

	@Shadow @Final private Map<SoundInstance, String> channelsByEvent;
	@Shadow private SoundEngine.System system;

	@Shadow protected abstract float getPitch(SoundInstance soundInstance);

	@Unique
	private GSTpsModule gs_tpsModule;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		GSClientController client = GSClientController.getInstance();
		gs_tpsModule = client.getTpsModule();
		
		gs_tpsModule.addTpsListener(this);
		client.getSettingManager().addChangeListener(this);
	}

	@Unique
	private void updatePitch() {
		// Note: pitch is updated accordingly inside of each source.
		for (Map.Entry<SoundInstance, String> soundEntry : channelsByEvent.entrySet()) {
			float pitch = getPitch(soundEntry.getKey());
			system.setPitch(soundEntry.getValue(), pitch);
		}
	}
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		updatePitch();
	}
	
	@Override
	public void onSettingChanged(GSSettingCategory category, GSSetting<?> setting) {
		if (setting == gs_tpsModule.cShiftPitch)
			updatePitch();
	}
}
