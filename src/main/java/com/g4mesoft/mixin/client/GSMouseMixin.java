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
			
			if (action == GLFW.GLFW_RELEASE) {
				// Make sure we don't get ghosting.
				GSControllerClient.getInstance().getKeyManager().onMouseReleased(button, mods);
			}
		}
	}

	@Inject(method="onMouseButton(JIII)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"))
	public void onMouseEventHandled(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		if (action == GLFW.GLFW_PRESS)
			GSControllerClient.getInstance().getKeyManager().onMousePressed(button, mods);
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
