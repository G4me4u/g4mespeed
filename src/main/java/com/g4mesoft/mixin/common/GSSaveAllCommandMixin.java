package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.server.GSServerController;

import net.minecraft.server.commands.SaveAllCommand;

@Mixin(SaveAllCommand.class)
public class GSSaveAllCommandMixin {

	@Inject(
		method = "saveAll",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/server/MinecraftServer;saveEverything(" +
					"Z" +
					"Z" +
					"Z" +
				")Z"
		)
	)
	private static void onSaveAllAfterSaveEverything(CallbackInfoReturnable<Integer> cir) {
		GSServerController.getInstance().autoSave();
	}
}
