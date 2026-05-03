package com.g4mesoft.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.setting.GSISettingChangeListener;
import com.g4mesoft.setting.GSSetting;
import com.g4mesoft.setting.GSSettingCategory;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.util.Mth;

@Mixin(SoundEngine.class)
public abstract class GSSoundEngineMixin implements GSITpsDependant, GSISettingChangeListener {

	@Shadow @Final private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;

	@Shadow protected abstract float calculatePitch(SoundInstance soundInstance);
	
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
	
	@Inject(
		method = "calculatePitch",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onGetAdjustedPitch(SoundInstance soundInstance, CallbackInfoReturnable<Float> cir) {
		float pitch = Mth.clamp(soundInstance.getPitch(), 0.5f, 2.0f);
		
		if (gs_tpsModule.cShiftPitch.get()) {
			// Scale pitch by relative tps difference to the default.
			pitch *= gs_tpsModule.getTps() / GSTpsModule.DEFAULT_TPS;
		}

		cir.setReturnValue(pitch);
		cir.cancel();
	}

	@Unique
	private void updatePitch() {
		for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> soundEntry : instanceToChannel.entrySet()) {
			float pitch = calculatePitch(soundEntry.getKey());
			soundEntry.getValue().execute((s) -> s.setPitch(pitch));
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
