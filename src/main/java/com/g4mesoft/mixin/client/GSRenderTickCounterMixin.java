package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSServerTickTimer;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class GSRenderTickCounterMixin implements GSITickTimer {

	@Shadow public int ticksThisFrame;
	@Shadow public float tickDelta;
	@Shadow public float lastFrameDuration;
	@Shadow public long prevTimeMillis;
	@Shadow @Final private float tickTime;

	private boolean firstUpdate;
	private GSCarpetCompat carpetCompat;
	private GSTpsModule tpsModule;
	private GSServerTickTimer serverTimer;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(float ticksPerSecond, long initialTimeMillis, CallbackInfo ci) {
		firstUpdate = true;
	}

	@Inject(method = "beginRenderTick", at = @At(value = "FIELD", shift = Shift.AFTER, opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/render/RenderTickCounter;lastFrameDuration:F"))
	private void onModifyTickrate(long timeMillis, CallbackInfo ci) {
		if (firstUpdate) {
			init(prevTimeMillis);
			firstUpdate = false;
		}
		
		float millisPerTick = getMillisPerTick();
		
		if (GSControllerClient.getInstance().isG4mespeedServer()) {
			serverTimer.setMillisPerTick(millisPerTick);
		} else {
			serverTimer.setMillisPerTick(DEFAULT_MILLIS_PER_TICK);
		}
		
		if (!carpetCompat.isTickrateLinked() || tpsModule.cForceCarpetTickrate.getValue())
			this.lastFrameDuration = (timeMillis - this.prevTimeMillis) / millisPerTick;
	}

	@Inject(method = "beginRenderTick", at = @At("RETURN"))
	private void onBeginRenderTick(long timeMillis, CallbackInfo ci) {
		update(timeMillis);
	}

	@Override
	public void init(long initialTimeMillis) {
		carpetCompat = G4mespeedMod.getInstance().getCarpetCompat();
		tpsModule = GSControllerClient.getInstance().getTpsModule();
		serverTimer = tpsModule.getServerTimer();
		
		serverTimer.init(initialTimeMillis);
	}

	@Override
	public void update(long timeMillis) {
		serverTimer.update(timeMillis);
		serverTimer.syncTimer(this);
	}

	@Override
	public float getMillisPerTick() {
		// Other mods such as the ReplayMod modify the timeScale value
		// of the timer. To ensure that the functionality stays as expected,
		// scale the milliseconds per tick by that value.
		return tpsModule.getMsPerTick() * tickTime / DEFAULT_MILLIS_PER_TICK;
	}

	@Override
	public float getTickDelta() {
		return tickDelta;
	}

	@Override
	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	@Override
	public int getTickCount() {
		return ticksThisFrame;
	}

	@Override
	public void setTickCount(int tickCount) {
		this.ticksThisFrame = tickCount;
	}
}
