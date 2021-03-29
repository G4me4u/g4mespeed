package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIMouseAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public class GSMouseMixin implements GSIMouseAccess {

	@Shadow @Final private MinecraftClient client;

	private int prevEventModifiers;
	private float prevEventScrollX;

	@Inject(method="onMouseButton(JIII)V", at = @At(value = "HEAD"))
	public void onMouseEvent(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		if (windowHandle == client.getWindow().getHandle()) {
			prevEventModifiers = mods;

			GSKeyManager keyManager = GSControllerClient.getInstance().getKeyManager();

			keyManager.clearEventQueue();
			if (action == GLFW.GLFW_RELEASE) {
				keyManager.onMouseReleased(button, mods);
			} else if (action == GLFW.GLFW_PRESS) {
				keyManager.onMousePressed(button, mods);
			}
		}
	}

	@Inject(method="onMouseButton(JIII)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/options/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$KeyCode;Z)V"))
	public void onMouseEventHandled(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		GSKeyManager keyManager = GSControllerClient.getInstance().getKeyManager();

		if (action == GLFW.GLFW_RELEASE) {
			keyManager.dispatchEvents(GSEKeyEventType.RELEASE);
		} else if (action == GLFW.GLFW_PRESS) {
			keyManager.dispatchEvents(GSEKeyEventType.PRESS);
		}
	}
	
	@Inject(method="onMouseScroll", at = @At(value = "HEAD"))
	private void onOnMouseScroll(long windowHandle, double scrollX, double scrollY, CallbackInfo ci) {
		if (windowHandle == client.getWindow().getHandle()) {
			prevEventScrollX = (float)(client.options.discreteMouseScroll ? Math.signum(scrollX) : scrollX);
			prevEventScrollX *= client.options.mouseWheelSensitivity;
		}
	}
	
	@Override
	public int getPreviousEventModifiers() {
		return prevEventModifiers;
	}

	@Override
	public double getPreviousEventScrollX() {
		return prevEventScrollX;
	}
}
