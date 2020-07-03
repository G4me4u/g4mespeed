package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIKeyboardAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.gui.GSScreen;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;

@Mixin(Keyboard.class)
public class GSKeyboardMixin implements GSIKeyboardAccess {
	
	@Shadow @Final private MinecraftClient client;
	@Shadow private boolean repeatEvents;

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
	
	/* Note that we are actually targeting the lambda method in onKey
	 * which is used in the Screen.wrapScreenError(...) method. */
	@Redirect(method = "method_1454", require = 0, allow = 1, at = @At(value = "FIELD", 
			opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/Keyboard;repeatEvents:Z"))
	public boolean allowRepeatEvents(Keyboard keyboard) {
		return (repeatEvents || (client.currentScreen instanceof GSScreen));
	}
	
	public GSKeyManager getKeyManager() {
		return GSControllerClient.getInstance().getKeyManager();
	}

	@Override
	public boolean isRepeatingKeyEvent() {
		return repeatingKeyEvent;
	}
}
