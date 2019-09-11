package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSControllerClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
@Environment(EnvType.CLIENT)
public class GSKeyboardMixin {

	private static final int KEY_RELEASE = 0;
	private static final int KEY_PRESS   = 1;
	private static final int KEY_REPEAT  = 2;
	
	@Shadow @Final private MinecraftClient client;
	
	@Inject(method="onKey(L;IIII)V", at = @At("RETURN"))
	public void onKeyEvent(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		G4mespeedMod gsInstance = G4mespeedMod.getInstance();
		if (gsInstance == null)
			return;
		
		if (client.window.getHandle() == windowHandle && gsInstance.getSettings().isEnabled()) {
			GSControllerClient gsClient = GSControllerClient.getInstance();
			
			switch (action) {
			case KEY_RELEASE:
				gsClient.keyReleased(key, scancode, mods);
				break;
			case KEY_PRESS:
				gsClient.keyPressed(key, scancode, mods);
				break;
			case KEY_REPEAT:
				gsClient.keyRepeat(key, scancode, mods);
				break;
			}
		}
	}
}
