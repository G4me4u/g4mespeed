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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;

@Mixin(Mouse.class)
public class GSMouseMixin {

	@Shadow @Final private MinecraftClient client;

	@Inject(
		method="onMouseButton(JLnet/minecraft/client/input/MouseInput;I)V",
		at = @At("HEAD")
	)
	private void onMouseEvent(long windowHandle, MouseInput input, int action, CallbackInfo ci) {
		if (windowHandle == client.getWindow().getHandle()) {
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
		method="onMouseButton(JLnet/minecraft/client/input/MouseInput;I)V",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.AFTER, 
			target =
				"Lnet/minecraft/client/option/KeyBinding;setKeyPressed(" +
					"Lnet/minecraft/client/util/InputUtil$Key;" +
					"Z" +
				")V"
		)
	)
	private void onMouseEventHandled(long windowHandle, MouseInput input, int action, CallbackInfo ci) {
		GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

		if (action == GLFW.GLFW_RELEASE) {
			keyManager.dispatchEvents(GSEKeyEventType.RELEASE);
		} else if (action == GLFW.GLFW_PRESS) {
			keyManager.dispatchEvents(GSEKeyEventType.PRESS);
		}
	}
}
