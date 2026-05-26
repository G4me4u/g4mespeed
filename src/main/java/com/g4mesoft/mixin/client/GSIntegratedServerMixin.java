package com.g4mesoft.mixin.client;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSServerController;

import net.minecraft.client.server.IntegratedServer;

@Mixin(IntegratedServer.class)
public class GSIntegratedServerMixin {

	@Shadow private boolean paused;
	
	@Inject(
		method = "tickServer",
		at = @At("RETURN")
	)
	private void onTickServer(BooleanSupplier booleanSupplier, CallbackInfo ci) {
		if (this.paused) {
			// At this point the client is paused and the tick method of
			// MinecraftServer was not called. Hence we have to call the
			// method ourselves to ensure that the modules receive the tick.
			GSServerController.getInstance().tick(true);
		}
	}

	@Inject(
		method = "tickServer",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.AFTER,
			target =
				"Lnet/minecraft/client/server/IntegratedServer;saveEverything(" +
					"Z" +
					"Z" +
					"Z" +
				")Z"
		)
	)
	private void onAutoSaveAfterSaveEverything(CallbackInfo ci) {
		GSServerController.getInstance().autoSave();
	}
}
