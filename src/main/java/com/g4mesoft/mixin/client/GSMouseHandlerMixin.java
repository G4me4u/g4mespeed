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

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public class GSMouseHandlerMixin {

	@Shadow @Final private Minecraft minecraft;

	@Inject(
		method="onPress(JIII)V",
		at = @At("HEAD")
	)
	private void onMouseEvent(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		if (windowHandle == minecraft.window.getWindow()) {
			GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

			keyManager.clearEventQueue();
			if (action == GLFW.GLFW_RELEASE) {
				keyManager.onMouseReleased(button, mods);
			} else if (action == GLFW.GLFW_PRESS) {
				keyManager.onMousePressed(button, mods);
			}
		}
	}

	@Inject(
		method="onPress(JIII)V",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.AFTER, 
			target =
				"Lnet/minecraft/client/options/KeyBinding;set(" +
					"Lcom/mojang/blaze3d/platform/InputConstants$Key;" +
					"Z" +
				")V"
		)
	)
	private void onMouseEventHandled(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

		if (action == GLFW.GLFW_RELEASE) {
			keyManager.dispatchEvents(GSEKeyEventType.RELEASE);
		} else if (action == GLFW.GLFW_PRESS) {
			keyManager.dispatchEvents(GSEKeyEventType.PRESS);
		}
	}
}
