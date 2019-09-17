package com.g4mesoft.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.GSISoundSystemAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.MathHelper;

@Mixin(SoundSystem.class)
@Implements(@Interface(iface = GSISoundSystemAccess.class, prefix = "tickAccess$"))
public abstract class GSSoundSystemMixin implements GSITpsDependant {

	private final Object sourcesLock = new Object();
	
	@Shadow @Final private Map<SoundInstance, Channel.SourceManager> sources;

	@Overwrite
	private float getAdjustedPitch(SoundInstance soundInstance) {
		GSControllerClient controller = GSControllerClient.getInstance();

		float pitch = MathHelper.clamp(soundInstance.getPitch(), 0.5F, 2.0F);
		if (controller.getClientSettings().isShiftPitchEnabled())
			return pitch * controller.getTpsModule().getTps() / GSTpsModule.DEFAULT_TPS;

		return pitch;
	}

	@Shadow public abstract void tick(boolean skipSoundTick);

	@Shadow public abstract void stopAll();
	
	@Shadow public abstract void play(SoundInstance soundInstance_1);
	
	@Intrinsic(displace = true)
	public void tickAccess$tick(boolean skipSoundTick) {
		synchronized (sourcesLock) {
			this.tick(skipSoundTick);
		}
	}
	
	@Intrinsic(displace = true)
	public void tickAccess$stopAll() {
		synchronized (sourcesLock) {
			this.stopAll();
		}
	}

	@Intrinsic(displace = true)
	public void tickAccess$play(SoundInstance soundInstance) {
		synchronized (sourcesLock) {
			this.play(soundInstance);
		}
	}
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		synchronized (sourcesLock) {
			for (Map.Entry<SoundInstance, Channel.SourceManager> soundEntry : sources.entrySet()) {
				float pitch = getAdjustedPitch(soundEntry.getKey());
				soundEntry.getValue().run((s) -> s.setPitch(pitch));
			}
		}
	}
}
