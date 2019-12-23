package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIRenderTickCounterAccess;
import com.g4mesoft.module.tps.GSRenderTickCounterAdjuster;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class GSRenderTickCounterMixin implements GSIRenderTickCounterAccess {

	@Shadow @Final private float timeScale;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(float tps, long currentMs, CallbackInfo ci) {
		GSRenderTickCounterAdjuster.getInstance().init(tps, currentMs);
	}
	
	@Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;timeScale:F"))
	private float getMsPerTick(RenderTickCounter counter) {
		return GSRenderTickCounterAdjuster.getInstance().getAdjustedMsPerTick((RenderTickCounter)(Object)this);
	}

	@Inject(method = "beginRenderTick", at = @At("RETURN"))
	private void onBeginRenderTick(long currentTimeMillis, CallbackInfo ci) {
		GSRenderTickCounterAdjuster.getInstance().performSynchronization((RenderTickCounter)(Object)this, currentTimeMillis);
	}

	@Override
	public float getTimeScale() {
		return timeScale;
	}
}
