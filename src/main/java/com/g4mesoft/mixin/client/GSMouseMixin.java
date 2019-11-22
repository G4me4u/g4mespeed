package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public class GSMouseMixin {

	private GSKeyManager keyManager;
	
	@Inject(method="onMouseButton(JIII)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/options/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$KeyCode;Z)V"))
	public void onMouseEvent(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		switch (action) {
		case GLFW.GLFW_PRESS:
			getKeyManager().onMousePressed(button, mods);
			break;
		case GLFW.GLFW_RELEASE:
			getKeyManager().onMouseReleased(button, mods);
			break;
		}
	}
	
	
	public GSKeyManager getKeyManager() {
		if (keyManager == null)
			keyManager = GSControllerClient.getInstance().getKeyManager();
		return keyManager;
	}
}
