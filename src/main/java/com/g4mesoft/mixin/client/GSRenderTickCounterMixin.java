package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSRenderTickCounterAdjuster;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class GSRenderTickCounterMixin {

	@Shadow public float lastFrameDuration;
	@Shadow public long prevTimeMillis;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(float tps, long currentMs, CallbackInfo ci) {
		GSRenderTickCounterAdjuster.getInstance().init(tps, currentMs);
	}

	@Inject(method = "beginRenderTick", at = @At(value = "FIELD", shift = Shift.AFTER, opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/render/RenderTickCounter;lastFrameDuration:F"))
	private void onModifyTickrate(long currentTimeMillis, CallbackInfo ci) {
		if (!G4mespeedMod.getInstance().getCarpetCompat().isTickrateLinked() || 
				GSControllerClient.getInstance().getTpsModule().cForceCarpetTickrate.getValue()) {
			
			float mspt = GSRenderTickCounterAdjuster.getInstance().getAdjustedMsPerTick((RenderTickCounter)(Object)this);
			this.lastFrameDuration = (float)(currentTimeMillis - this.prevTimeMillis) / mspt;
		}
	}

	@Inject(method = "beginRenderTick", at = @At("RETURN"))
	private void onBeginRenderTick(long currentTimeMillis, CallbackInfo ci) {
		GSRenderTickCounterAdjuster.getInstance().performSynchronization((RenderTickCounter)(Object)this, currentTimeMillis);
	}
}
