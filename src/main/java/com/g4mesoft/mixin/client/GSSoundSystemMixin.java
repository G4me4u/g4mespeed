package com.g4mesoft.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.GSControllerClient;
import com.g4mesoft.tps.GSITpsDependant;
import com.g4mesoft.tps.GSTpsManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.MathHelper;

@Mixin(SoundSystem.class)
@Environment(EnvType.CLIENT)
public class GSSoundSystemMixin implements GSITpsDependant {

	@Shadow @Final private Map<SoundInstance, Channel.SourceManager> sources;

	@Overwrite
	private float getAdjustedPitch(SoundInstance soundInstance) {
		G4mespeedMod gsInstance = G4mespeedMod.getInstance();

		float pitch = MathHelper.clamp(soundInstance.getPitch(), 0.5F, 2.0F);
		if (gsInstance.getSettings().isShiftPitchEnabled()) {
			GSTpsManager tpsManager = GSControllerClient.getInstance().getTpsManager();
			return pitch * tpsManager.getTps() / GSTpsManager.DEFAULT_TPS;
		}

		return pitch;
	}

	@Override
	public void tpsChanged(float newTps, float oldTps) {
		for (Map.Entry<SoundInstance, Channel.SourceManager> soundEntry : sources.entrySet()) {
			float pitch = getAdjustedPitch(soundEntry.getKey());
			soundEntry.getValue().run((s) -> s.setPitch(pitch));
		}
	}
}
