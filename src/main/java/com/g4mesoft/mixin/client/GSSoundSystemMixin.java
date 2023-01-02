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

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.math.MathHelper;

@Mixin(SoundSystem.class)
public abstract class GSSoundSystemMixin implements GSITpsDependant, GSISettingChangeListener {

	@Shadow @Final private Map<SoundInstance, Channel.SourceManager> sources;

	@Shadow protected abstract float getAdjustedPitch(SoundInstance soundInstance);
	
	@Unique
	private GSTpsModule gs_tpsModule;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(SoundManager loader, GameOptions settings, ResourceFactory resourceFactory, CallbackInfo ci) {
		GSClientController client = GSClientController.getInstance();
		gs_tpsModule = client.getTpsModule();
		
		gs_tpsModule.addTpsListener(this);
		client.getSettingManager().addChangeListener(this);
	}
	
	@Inject(method = "getAdjustedPitch", cancellable = true, at = @At("HEAD"))
	private void onGetAdjustedPitch(SoundInstance soundInstance, CallbackInfoReturnable<Float> cir) {
		float pitch = MathHelper.clamp(soundInstance.getPitch(), 0.5f, 2.0f);
		
		if (gs_tpsModule.cShiftPitch.get()) {
			// Scale pitch by relative tps difference to the default.
			pitch *= gs_tpsModule.getTps() / GSTpsModule.DEFAULT_TPS;
		}

		cir.setReturnValue(pitch);
		cir.cancel();
	}

	@Unique
	private void updatePitch() {
		for (Map.Entry<SoundInstance, Channel.SourceManager> soundEntry : sources.entrySet()) {
			float pitch = getAdjustedPitch(soundEntry.getKey());
			soundEntry.getValue().run((s) -> s.setPitch(pitch));
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
