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
import net.minecraft.client.input.MouseButtonInfo;

@Mixin(MouseHandler.class)
public class GSMouseHandlerMixin {

	@Shadow @Final private Minecraft minecraft;

	@Inject(
		method=
			"onButton(" +
				"J" +
				"Lnet/minecraft/client/input/MouseButtonInfo;" +
				"I" +
			")V",
		at = @At("HEAD")
	)
	private void onOnButton(long windowHandle, MouseButtonInfo input, int action, CallbackInfo ci) {
		if (windowHandle == minecraft.getWindow().handle()) {
			GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

			keyManager.clearEventQueue();
			if (action == GLFW.GLFW_RELEASE) {
				keyManager.onMouseReleased(input);
			} else if (action == GLFW.GLFW_PRESS) {
				keyManager.onMousePressed(input);
			}
		}
	}

	@Inject(
		method=
			"onButton(" +
				"J" +
				"Lnet/minecraft/client/input/MouseButtonInfo;" +
				"I" +
			")V",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.AFTER, 
			target =
				"Lnet/minecraft/client/KeyMapping;set(" +
					"Lcom/mojang/blaze3d/platform/InputConstants$Key;" +
					"Z" +
				")V"
		)
	)
	private void onOnButtonAfterHandled(long windowHandle, MouseButtonInfo input, int action, CallbackInfo ci) {
		GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

		if (action == GLFW.GLFW_RELEASE) {
			keyManager.dispatchEvents(GSEKeyEventType.RELEASE);
		} else if (action == GLFW.GLFW_PRESS) {
			keyManager.dispatchEvents(GSEKeyEventType.PRESS);
		}
	}
}
