package com.g4mesoft.mixin.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.hotkey.GSEKeyEventType;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

@Mixin(KeyboardHandler.class)
public class GSKeyboardHandlerMixin {
	
	@Shadow @Final private Minecraft minecraft;
	
	@Inject(
		method =
			"keyPress(" +
				"J" +
				"I" +
				"Lnet/minecraft/client/input/KeyEvent;" +
			")V",
		at = @At("HEAD")
	)
	private void onKeyPress(long windowHandle, int action, KeyEvent input, CallbackInfo ci) {
		if (windowHandle == minecraft.getWindow().handle()) {
			GSKeyManager keyManager = GSClientController.getInstance().getKeyManager();

			keyManager.clearEventQueue();
			if (action == GLFW.GLFW_RELEASE) {
				keyManager.onKeyReleased(input);
			} else if (action == GLFW.GLFW_PRESS) {
				keyManager.onKeyPressed(input);
			}
		}
	}

	@Inject(
		method =
			"keyPress(" +
				"J" +
				"I" +
				"Lnet/minecraft/client/input/KeyEvent;" +
			")V",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				shift = Shift.AFTER,
				target =
					"Lcom/mojang/blaze3d/platform/InputConstants;getKey(" +
						"Lnet/minecraft/client/input/KeyEvent;" +
					")Lcom/mojang/blaze3d/platform/InputConstants$Key;"
			)
		),
		at = @At(
			value = "INVOKE",
			ordinal = 0,
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/client/KeyMapping;set(" +
					"Lcom/mojang/blaze3d/platform/InputConstants$Key;" +
					"Z" +
				")V"
		)
	)
	private void onKeyReleased(long windowHandle, int action, KeyEvent input, CallbackInfo ci) {
		GSClientController.getInstance().getKeyManager().dispatchEvents(GSEKeyEventType.RELEASE);
	}

	@Inject(
		method =
			"keyPress(" +
				"J" +
				"I" +
				"Lnet/minecraft/client/input/KeyEvent;" +
			")V",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				shift = Shift.AFTER,
				target =
					"Lcom/mojang/blaze3d/platform/InputConstants;getKey(" +
						"Lnet/minecraft/client/input/KeyEvent;" +
					")Lcom/mojang/blaze3d/platform/InputConstants$Key;"
			)
		),
		at = @At(
			value = "INVOKE",
			shift = At.Shift.BEFORE, 
			target =
				"Lnet/minecraft/client/KeyMapping;click(" +
					"Lcom/mojang/blaze3d/platform/InputConstants$Key;" +
				")V"
		)
	)
	private void onKeyPressRepeat(long windowHandle, int action, KeyEvent input, CallbackInfo ci) {
		if (action == GLFW.GLFW_PRESS)
			GSClientController.getInstance().getKeyManager().dispatchEvents(GSEKeyEventType.PRESS);
	}
}
