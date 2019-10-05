package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public class GSKeyboardMixin {

	private static final int KEY_RELEASE = 0;
	private static final int KEY_PRESS   = 1;
	private static final int KEY_REPEAT  = 2;
	
	@Shadow @Final private MinecraftClient client;
	
	@Inject(method="onKey(JIIII)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/options/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$KeyCode;)V"))
	public void onKeyEvent(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		if (client.window.getHandle() == windowHandle) {
			switch (action) {
			case KEY_RELEASE:
				GSControllerClient.getInstance().keyReleased(key, scancode, mods);
				break;
			case KEY_PRESS:
				GSControllerClient.getInstance().keyPressed(key, scancode, mods);
				break;
			case KEY_REPEAT:
				GSControllerClient.getInstance().keyRepeat(key, scancode, mods);
				break;
			}
		}
	}
}
