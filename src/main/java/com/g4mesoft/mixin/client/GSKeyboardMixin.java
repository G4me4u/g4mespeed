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
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public class GSKeyboardMixin implements GSIKeyboardAccess {
	
	@Shadow @Final private MinecraftClient client;

	private boolean repeatingKeyEvent;
	
	@Inject(method = "onKey(JIIII)V", at = @At("HEAD"))
	public void onKeyEvent(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		if (windowHandle == client.window.getHandle()) {
			repeatingKeyEvent = (action == GLFW.GLFW_REPEAT);
			
			if (action == GLFW.GLFW_RELEASE)
				getKeyManager().onKeyReleased(key, scancode, mods);
		}
	}

	@Inject(method = "onKey(JIIII)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/options/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$KeyCode;)V"))
	public void onKeyPressRepeat(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		switch (action) {
		case GLFW.GLFW_PRESS:
			getKeyManager().onKeyPressed(key, scancode, mods);
			break;
		case GLFW.GLFW_REPEAT:
			getKeyManager().onKeyRepeat(key, scancode, mods);
			break;
		}
	}
	
	public GSKeyManager getKeyManager() {
		return GSControllerClient.getInstance().getKeyManager();
	}

	@Override
	public boolean isRepeatingKeyEvent() {
		return repeatingKeyEvent;
	}
}
