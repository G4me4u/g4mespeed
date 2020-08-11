package com.g4mesoft.mixin.client;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSControllerServer;

import net.minecraft.server.integrated.IntegratedServer;

@Mixin(IntegratedServer.class)
public class GSIntegratedServerMixin {

	@Shadow private boolean paused;
	
	@Inject(method = "tick", at = @At("RETURN"))
	private void onTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
		if (this.paused) {
			// At this point the client is paused and the tick method of
			// MinecraftServer was not called. Hence we have to call the
			// method ourselves to ensure that the modules receive the tick.
			GSControllerServer.getInstance().tick(true);
		}
	}
}
