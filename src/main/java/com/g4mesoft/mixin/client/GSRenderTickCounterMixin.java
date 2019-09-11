package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSControllerClient;
import com.g4mesoft.tickspeed.GSTpsManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
@Environment(EnvType.CLIENT)
public class GSRenderTickCounterMixin {

	private static final float MS_PER_SEC = 1000.0f;

	@Shadow @Final private float timeScale;

	@Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;timeScale:F"))
	private float getMsPerTick(RenderTickCounter counter) {
		G4mespeedMod gsInstance = G4mespeedMod.getInstance();
		if (gsInstance.getSettings().isEnabled()) {
			GSTpsManager tpsManager = GSControllerClient.getInstance().getTpsManager();
			return MS_PER_SEC / tpsManager.getTps();
		}
		return timeScale;
	}
}
