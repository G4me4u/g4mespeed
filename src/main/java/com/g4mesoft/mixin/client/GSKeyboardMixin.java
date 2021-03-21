package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIKeyboardAccess;
import com.g4mesoft.core.client.GSControllerClient;

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
			
			if (action == GLFW.GLFW_RELEASE) {
				// Sometimes keys can get stuck. Make sure we do not
				// get key ghosting. This usually happens if a key
				// opens a GUI.
				GSControllerClient.getInstance().getKeyManager().onKeyReleased(key, scancode, mods);
			}
		}
	}

	@Inject(method="onKey(JIIII)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/option/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V"))
	public void onKeyPressRepeat(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		switch (action) {
		case GLFW.GLFW_PRESS:
			GSControllerClient.getInstance().getKeyManager().onKeyPressed(key, scancode, mods);
			break;
		case GLFW.GLFW_REPEAT:
			GSControllerClient.getInstance().getKeyManager().onKeyRepeat(key, scancode, mods);
			break;
		}
	}
	
	@Override
	public boolean isPreviousEventRepeating() {
		return prevEventRepeating;
	}
}
