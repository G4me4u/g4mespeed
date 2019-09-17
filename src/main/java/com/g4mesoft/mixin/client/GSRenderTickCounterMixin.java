package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.access.GSIRenderTickAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.utils.GSMathUtils;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class GSRenderTickCounterMixin implements GSIRenderTickAccess, GSITpsDependant {

	private static final float DEFAULT_MS_PER_TICK = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;
	
	private static final float SYNC_EASING_MULTIPLIER = 0.05f;
	private static final float SERVER_SYNC_DELAY = 5.0f;
	
	@Shadow public int ticksThisFrame;
	@Shadow public float tickDelta;
	@Shadow public float lastFrameDuration;
	@Shadow private long prevTimeMillis;
	@Shadow @Final private float timeScale;
	
	private float approximatedServerTickDelta;
	private boolean serverSyncReceived;
	private int serverTicksSinceLastSync;
	private int serverSyncInterval;
	
	@Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;timeScale:F"))
	private float getMsPerTick(RenderTickCounter counter) {
		if (G4mespeedMod.getInstance().getSettings().isEnabled())
			return GSControllerClient.getInstance().getTpsModule().getMsPerTick();
		return timeScale;
	}

	@Inject(method = "beginRenderTick", at = @At("RETURN"))
	private void onBeginRenderTick(long currentTimeMillis, CallbackInfo ci) {
		if (G4mespeedMod.getInstance().getSettings().isEnabled()) {
			GSControllerClient controllerClient = GSControllerClient.getInstance();
			if (controllerClient.isG4mespeedServer()) {
				// Assume the server has the same tps
				approximatedServerTickDelta += lastFrameDuration;
			} else {
				// Assume the server has DEFAULT_TPS (20) tps
				approximatedServerTickDelta += (float)(currentTimeMillis - this.prevTimeMillis) / DEFAULT_MS_PER_TICK;
			}
			
			int serverTicksThisFrame = (int)approximatedServerTickDelta;
			approximatedServerTickDelta -= serverTicksThisFrame;
			serverTicksSinceLastSync += serverTicksThisFrame;
			
			if (shouldAdjustTickDelta())
				adjustTickDelta();
			
			if (serverTicksSinceLastSync >= serverSyncInterval * 2)
				serverSyncReceived = false;
		}
	}

	private boolean shouldAdjustTickDelta() {
		if (GSControllerClient.getInstance().isG4mespeedServer())
			return true;
		
		if (!serverSyncReceived)
			return false;
		
		float tps = GSControllerClient.getInstance().getTpsModule().getTps();
		return GSMathUtils.equalsApproximate(tps, GSTpsModule.DEFAULT_TPS);
	}
	
	private void adjustTickDelta() {
		float delay = SERVER_SYNC_DELAY / GSControllerClient.getInstance().getTpsModule().getMsPerTick();
		
		float targetTickDelta = approximatedServerTickDelta + delay;
		if (targetTickDelta > 1.0f)
			targetTickDelta--;
		
		// Check if we have to cross tick border
		// and adjust target value accordingly.
		float tickDeltaDif = this.tickDelta - targetTickDelta;
		if (tickDeltaDif > 0.5f) {
			targetTickDelta++;
		} else if (tickDeltaDif < -0.5f){
			targetTickDelta--;
		}
		
		this.tickDelta += (targetTickDelta - this.tickDelta) * SYNC_EASING_MULTIPLIER;
		
		if (this.tickDelta < 0.0f) {
			if (this.ticksThisFrame > 0) {
				this.ticksThisFrame--;
				this.tickDelta++;
			} else {
				this.tickDelta = 0.0f;
			}
		} else if (this.tickDelta >= 1.0f) {
			this.ticksThisFrame++;
			this.tickDelta--;
		}
	}
	
	private void resetServerApproximation() {
		approximatedServerTickDelta = 0.0f;
		serverTicksSinceLastSync = 0;
	}
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		// Resetting tickDelta will sync the server and 
		// client when tps changes (server does the same).
		if (GSControllerClient.getInstance().isG4mespeedServer())
			resetServerApproximation();
	}

	@Override
	public void onServerTickSync(int syncInterval) {
		resetServerApproximation();
		
		serverSyncInterval = syncInterval;
		serverSyncReceived = true;
	}
}
