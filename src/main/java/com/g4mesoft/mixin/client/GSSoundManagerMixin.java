package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.tps.GSITpsDependant;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;

@Mixin(SoundManager.class)
@Environment(EnvType.CLIENT)
public class GSSoundManagerMixin implements GSITpsDependant {

	@Shadow @Final private SoundSystem soundSystem;
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		((GSITpsDependant)soundSystem).tpsChanged(newTps, oldTps);
	}
}
