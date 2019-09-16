package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSControllerClient;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class GSRenderTickCounterMixin {

	@Shadow @Final private float timeScale;

	@Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;timeScale:F"))
	private float getMsPerTick(RenderTickCounter counter) {
		G4mespeedMod gsInstance = G4mespeedMod.getInstance();
		if (gsInstance.getSettings().isEnabled())
			return GSControllerClient.getInstance().getTpsModule().getMsPerTick();
		return timeScale;
	}
}
