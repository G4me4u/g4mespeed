package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.gui.GSElementContext;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public class GSMouseMixin {

	@Shadow @Final private MinecraftClient client;

	@Shadow private int activeButton;
	@Shadow private double glfwTime;
	@Shadow private boolean hasResolutionChanged;
	
	@Shadow private double x;
	@Shadow private double y;

	@Inject(method="onMouseButton(JIII)V", at = @At(value = "HEAD"))
	public void onMouseEvent(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		if (windowHandle == client.window.getHandle()) {
			float mouseX = getScaledMouseX((float)x);
			float mouseY = getScaledMouseY((float)y);
			
			switch (action) {
			case GLFW.GLFW_PRESS:
				GSElementContext.getEventDispatcher().mousePressed(button, mouseX, mouseY, mods);
				break;
			case GLFW.GLFW_RELEASE:
				GSElementContext.getEventDispatcher().mouseReleased(button, mouseX, mouseY, mods);
				
				// Make sure we don't get ghosting.
				getKeyManager().onMouseReleased(button, mods);
				break;
			}
		}
	}

	@Inject(method="onMouseButton(JIII)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/client/options/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$KeyCode;Z)V"))
	public void onMouseEventHandled(long windowHandle, int button, int action, int mods, CallbackInfo ci) {
		if (action == GLFW.GLFW_PRESS)
			getKeyManager().onMousePressed(button, mods);
	}
	
	@Inject(method="onMouseScroll", at = @At(value = "HEAD"))
	private void onOnMouseScroll(long windowHandle, double scrollX, double scrollY, CallbackInfo ci) {
		if (windowHandle == client.window.getHandle()) {
			if (client.options.discreteMouseScroll) {
				scrollX = Math.signum(scrollX);
				scrollY = Math.signum(scrollY);
			}

			scrollX *= client.options.mouseWheelSensitivity;
			scrollY *= client.options.mouseWheelSensitivity;
		
			float mouseX = getScaledMouseX((float)x);
			float mouseY = getScaledMouseY((float)y);
			
			GSElementContext.getEventDispatcher().mouseScroll(mouseX, mouseY, (float)scrollX, (float)scrollY);
		}
	}
	
	@Inject(method = "onCursorPos", at = @At(value = "HEAD"))
	private void onOnCursorPos(long windowHandle, double x, double y, CallbackInfo ci) {
		if (windowHandle == client.window.getHandle()) {
			float mouseX = getScaledMouseX((float)x);
			float mouseY = getScaledMouseY((float)y);
			
			GSElementContext.getEventDispatcher().mouseMoved(mouseX, mouseY);
			
			if (activeButton != -1 && glfwTime > 0.0) {
				float dragX;
				float dragY;
				
				if (hasResolutionChanged) {
					dragX = dragY = 0.0f;
				} else {
					dragX = getScaledMouseX((float)(x - this.x));
					dragY = getScaledMouseY((float)(y - this.y));
				}
			
				GSElementContext.getEventDispatcher().mouseDragged(activeButton, mouseX, mouseY, dragX, dragY);
			}
		}
	}
	
	private float getScaledMouseX(float mouseX) {
        return (mouseX * client.window.getScaledWidth()) / client.window.getWidth();
	}

	private float getScaledMouseY(float mouseY) {
		return (mouseY * client.window.getScaledHeight()) / client.window.getHeight();
	}
	
	public GSKeyManager getKeyManager() {
		return GSControllerClient.getInstance().getKeyManager();
	}
}
