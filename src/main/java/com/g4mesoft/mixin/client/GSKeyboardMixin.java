package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIKeyboardAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public class GSKeyboardMixin implements GSIKeyboardAccess {
	
	@Shadow @Final private MinecraftClient client;

	private boolean prevEventRepeating;
	
	@Inject(method = "onKey(JIIII)V", at = @At("HEAD"))
	public void onKeyEvent(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		if (windowHandle == client.getWindow().getHandle()) {
			prevEventRepeating = (action == GLFW.GLFW_REPEAT);

			GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

			keyManager.clearEventQueue();
			if (action == GLFW.GLFW_RELEASE) {
				keyManager.onKeyReleased(key, scancode, mods);
			} else if (action == GLFW.GLFW_PRESS) {
				keyManager.onKeyPressed(key, scancode, mods);
			}
		}
	}

	@Inject(method="onKey(JIIII)V", at = @At(value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"))
	public void onKeyReleased(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		GSClientController.getInstance().getKeyManager().dispatchEvents(GSEKeyEventType.RELEASE);
	}

	@Inject(method="onKey(JIIII)V", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, 
			target = "Lnet/minecraft/client/option/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V"))
	public void onKeyPressRepeat(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		if (action == GLFW.GLFW_PRESS)
			GSClientController.getInstance().getKeyManager().dispatchEvents(GSEKeyEventType.PRESS);
	}
	
	@Override
	public boolean isPreviousEventRepeating() {
		return prevEventRepeating;
	}
}
