package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;

@Mixin(KeyboardHandler.class)
public class GSKeyboardHandlerMixin {
	
	@Shadow @Final private Minecraft minecraft;
	
	@Inject(
		method = "keyPress(JIIII)V",
		at = @At("HEAD")
	)
	private void onKeyEvent(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		if (windowHandle == minecraft.window.getWindow()) {
			GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

			keyManager.clearEventQueue();
			if (action == GLFW.GLFW_RELEASE) {
				keyManager.onKeyReleased(key, scancode, mods);
			} else if (action == GLFW.GLFW_PRESS) {
				keyManager.onKeyPressed(key, scancode, mods);
			}
		}
	}

	@Inject(
		method="keyPress(JIIII)V",
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			shift = At.Shift.AFTER, 
			target =
				"Lnet/minecraft/client/options/KeyBinding;set(" +
					"Lcom/mojang/blaze3d/platform/InputConstants$Key;" +
					"Z" +
				")V"
		)
	)
	private void onKeyReleased(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		GSClientController.getInstance().getKeyManager().dispatchEvents(GSEKeyEventType.RELEASE);
	}

	@Inject(
		method="keyPress(JIIII)V",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.BEFORE, 
			target =
				"Lnet/minecraft/client/options/KeyBinding;click(" +
					"Lcom/mojang/blaze3d/platform/InputConstants$Key;" +
				")V"
		)
	)
	private void onKeyPressRepeat(long windowHandle, int key, int scancode, int action, int mods, CallbackInfo ci) {
		if (action == GLFW.GLFW_PRESS)
			GSClientController.getInstance().getKeyManager().dispatchEvents(GSEKeyEventType.PRESS);
	}
}
