package com.g4mesoft.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;
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
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathHelper;

@Mixin(SoundSystem.class)
public abstract class GSSoundSystemMixin implements GSITpsDependant, GSISettingChangeListener {

	@Shadow @Final private Map<SoundInstance, Channel.SourceManager> sources;

	private GSTpsModule tpsModule;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(SoundManager soundManager, GameOptions options, ResourceManager resourceManager, CallbackInfo ci) {
		GSControllerClient client = GSControllerClient.getInstance();
		tpsModule = client.getTpsModule();
		
		tpsModule.addTpsListener(this);
		client.getSettingManager().addChangeListener(this);
	}
	
	/**
	 * @author Christian
	 * @reason The whole method is changed to return shifted pitch
	 */
	@Overwrite
	private float getAdjustedPitch(SoundInstance soundInstance) {
		float pitch = MathHelper.clamp(soundInstance.getPitch(), 0.5F, 2.0F);
		if (tpsModule.cShiftPitch.getValue())
			return pitch * tpsModule.getTps() / GSTpsModule.DEFAULT_TPS;

		return pitch;
	}

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
		if (setting == tpsModule.cShiftPitch)
			updatePitch();
	}
}
