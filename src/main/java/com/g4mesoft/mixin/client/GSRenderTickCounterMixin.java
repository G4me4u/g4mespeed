package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIRenderTickAccess;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.SystemUtil;

@Mixin(RenderTickCounter.class)
public class GSRenderTickCounterMixin implements GSIRenderTickAccess, GSITpsDependant {

	private static final float DEFAULT_MS_PER_TICK = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;
	
	private static final float EXTRA_SERVER_SYNC_DELAY = 5.0f;
	private static final float MIN_SERVER_SYNC_DELAY = 10.0f;
	private static final float SYNC_DELAY_EASING_FACTOR = 0.05f;
	
	@Shadow public int ticksThisFrame;
	@Shadow public float tickDelta;
	@Shadow public float lastFrameDuration;
	@Shadow private long prevTimeMillis;
	@Shadow @Final private float timeScale;
	
	private float msPerTick = DEFAULT_MS_PER_TICK;
	
	private final Object serverSyncLock = new Object();
	private float approximatedServerTickDelta;
	private boolean serverSyncReceived;
	private long serverLast;
	private int serverTicksSinceLastSync;
	private int serverSyncInterval;

	private long clientLast;
	private float serverSyncDelay;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(float tps, long currentMs, CallbackInfo ci) {
		serverLast = clientLast = currentMs;
	}
	
	@Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;timeScale:F"))
	private float getMsPerTick(RenderTickCounter counter) {
		return msPerTick;
	}

	@Inject(method = "beginRenderTick", at = @At("RETURN"))
	private void onBeginRenderTick(long currentTimeMillis, CallbackInfo ci) {
		synchronized (serverSyncLock) {
			updateServerClock(currentTimeMillis);
			updateSyncDelay(currentTimeMillis);
			
			if (getTpsModule().cSyncTick.getValue() && shouldAdjustTickDelta())
				adjustTickDelta();
			
			if (serverTicksSinceLastSync >= serverSyncInterval * 2)
				serverSyncReceived = false;
		}
	}
	
	private void updateServerClock(long currentTimeMillis) {
		GSControllerClient controllerClient = GSControllerClient.getInstance();
		
		long deltaMsServer = currentTimeMillis - serverLast;
		serverLast = currentTimeMillis;
		
		if (controllerClient.isG4mespeedServer()) {
			// Assume the server has the same tps
			approximatedServerTickDelta += deltaMsServer / msPerTick;
		} else {
			// Assume the server has DEFAULT_TPS (20) tps
			approximatedServerTickDelta += (float)deltaMsServer / DEFAULT_MS_PER_TICK;
		}
		
		int serverTicksThisFrame = (int)approximatedServerTickDelta;
		approximatedServerTickDelta -= serverTicksThisFrame;
		serverTicksSinceLastSync += serverTicksThisFrame;
	}
	
	private void updateSyncDelay(long currentTimeMillis) {
		long deltaMs = currentTimeMillis - clientLast;
		clientLast = currentTimeMillis;

		float targetDelay = Math.max(MIN_SERVER_SYNC_DELAY, deltaMs + EXTRA_SERVER_SYNC_DELAY);
		serverSyncDelay += (targetDelay - serverSyncDelay) * SYNC_DELAY_EASING_FACTOR;
	}

	private boolean shouldAdjustTickDelta() {
		if (GSControllerClient.getInstance().isG4mespeedServer())
			return true;
		
		if (!serverSyncReceived)
			return false;
		
		return GSMathUtils.equalsApproximate(getTpsModule().getTps(), GSTpsModule.DEFAULT_TPS);
	}
	
	private void adjustTickDelta() {
		float targetTickDelta = approximatedServerTickDelta + serverSyncDelay / msPerTick;
		if (targetTickDelta > 1.0f)
			targetTickDelta--;
		
		// Check if we have to cross tick border
		// and adjust target value accordingly.
		float targetOffset = targetTickDelta - this.tickDelta;
		if (targetOffset < -0.5f) {
			targetOffset++;
		} else if (targetOffset > 0.5f){
			targetOffset--;
		}
		
		this.tickDelta += targetOffset * getTpsModule().cSyncTickAggression.getValue();
		
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
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		msPerTick = GSTpsModule.MS_PER_SEC / newTps;
	}

	@Override
	public void onServerTickSync(int syncInterval) {
		synchronized (serverSyncLock) {
			approximatedServerTickDelta = 0.0f;
			serverTicksSinceLastSync = 0;
			serverLast = SystemUtil.getMeasuringTimeMs();
			serverSyncInterval = syncInterval;
			serverSyncReceived = true;
		}
	}
	
	private GSTpsModule getTpsModule() {
		return GSControllerClient.getInstance().getTpsModule();
	}
}
