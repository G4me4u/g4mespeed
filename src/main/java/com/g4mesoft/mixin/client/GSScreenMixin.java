package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.hotkey.GSKeyManager;

import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public class GSScreenMixin {

	@Unique
	private GSKeyManager gs_keyManager;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_keyManager = GSClientController.getInstance().getKeyManager();
	}
	
	@Inject(
		method = "handleInputs",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/client/gui/screen/Screen;handleMouse(" +
				")V"
		)
	)
	private void onHandleInputsBeforeHandleMouse(CallbackInfo ci) {
		gs_keyManager.handleMouse();
	}

	@Inject(
		method = "handleInputs",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/client/gui/screen/Screen;handleKeyboard(" +
				")V"
		)
	)
	private void onHandleInputsBeforeHandleKeyboard(CallbackInfo ci) {
		gs_keyManager.handleKeyboard();
	}
}
