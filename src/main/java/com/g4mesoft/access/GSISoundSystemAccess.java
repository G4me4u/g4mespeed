package com.g4mesoft.access;

import net.minecraft.client.sound.SoundInstance;

public interface GSISoundSystemAccess {

	public void tick(boolean skipSoundTick);

	public void stopAll();
	
	public void play(SoundInstance soundInstance);
	
}
