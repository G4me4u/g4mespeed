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
import net.minecraft.util.Util;

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
	@Shadow @Final private float tickTime;
	
	private float msPerTick = DEFAULT_MS_PER_TICK;
	
	private final Object serverSyncLock = new Object();
	private float approximatedServerTickDelta;
	private boolean serverSyncReceived;
	private long serverLast;
	private int serverTicksSinceLastSync;
	private int serverSyncInterval;

	private long clientLast;
	private float serverSyncDelay;
	private boolean tickAfterServer;
	
	private GSTpsModule tpsModule;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(float tps, long currentMs, CallbackInfo ci) {
		serverLast = clientLast = currentMs;
		
		tpsModule = GSControllerClient.getInstance().getTpsModule();
		tpsModule.addTpsListener(this);
		
		// The only reason why we're ticking before the server is to
		// allow different animation types of pistons. But the pistons
		// can actually have pause at end whilst we are ticking after
		// the server. This allows for better looking sand animations.
		// 
		// However, it is not possible to reliably tick after the server
		// since this relies on how fast the server is running. This.
		// feature remains unused, but stays if needed in the future.
		tickAfterServer = false;
	}
	
	private float getAdjustedMsPerTick() {
		// Other mods such as the ReplayMod modify the
		// tickTime value of the timer. To ensure that
		// the functionality stays as expected, scale the
		// milliseconds per tick by that value.
		return msPerTick * tickTime / DEFAULT_MS_PER_TICK;
	}
	
	@Redirect(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;tickTime:F"))
	private float getMsPerTick(RenderTickCounter counter) {
		return getAdjustedMsPerTick();
	}

	@Inject(method = "beginRenderTick", at = @At("RETURN"))
	private void onBeginRenderTick(long currentTimeMillis, CallbackInfo ci) {
		synchronized (serverSyncLock) {
			updateServerClock(currentTimeMillis);
			updateSyncDelay(currentTimeMillis);
			
			if (tpsModule.cSyncTick.getValue() && shouldAdjustTickDelta())
				adjustTickDelta();
			
			if (serverTicksSinceLastSync >= serverSyncInterval * 2)
				serverSyncReceived = false;
		}
	}
	
	private void updateServerClock(long currentTimeMillis) {
		long deltaMsServer = currentTimeMillis - serverLast;
		serverLast = currentTimeMillis;
		
		if (GSControllerClient.getInstance().isG4mespeedServer()) {
			// Assume the server has the same tps
			approximatedServerTickDelta += deltaMsServer / getAdjustedMsPerTick();
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
		
		return GSMathUtils.equalsApproximate(tpsModule.getTps(), GSTpsModule.DEFAULT_TPS);
	}
	
	private void adjustTickDelta() {
		float targetTickDelta = approximatedServerTickDelta;
		if (tickAfterServer) {
			targetTickDelta -= serverSyncDelay / getAdjustedMsPerTick();
		} else {
			targetTickDelta += serverSyncDelay / getAdjustedMsPerTick();
		}
		
		targetTickDelta %= 1.0f;
		if (targetTickDelta < 0.0f)
			targetTickDelta++;
		
		// Check if we have to cross tick border
		// and adjust target value accordingly.
		float targetOffset = targetTickDelta - this.tickDelta;
		if (targetOffset < -0.5f) {
			targetOffset++;
		} else if (targetOffset > 0.5f){
			targetOffset--;
		}
		
		this.tickDelta += targetOffset * tpsModule.cSyncTickAggression.getValue();
		
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
			serverLast = Util.getMeasuringTimeMs();
			serverSyncInterval = syncInterval;
			serverSyncReceived = true;
		}
	}
}
