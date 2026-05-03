package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.access.client.GSIDeltaTrackerTimerAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSServerTickTimer;
import com.g4mesoft.module.tps.GSTpsModule;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.DeltaTracker;

@Mixin(DeltaTracker.Timer.class)
public class GSDeltaTrackerTimerMixin implements GSIDeltaTrackerTimerAccess, GSITickTimer {

	@Shadow public float deltaTickResidual;
	@Shadow public float deltaTicks;
	@Shadow public long lastMs;
	@Shadow @Final private float msPerTick;

	@Shadow private float realtimeDeltaTicks;
	@Shadow private float pausedDeltaTickResidual;
	@Shadow private long lastUiMs;

	@Unique
	private int gs_ticksThisFrame;
	
	@Unique
	private boolean gs_firstUpdate;
	@Unique
	private GSTpsModule gs_tpsModule;
	@Unique
	private GSServerTickTimer gs_serverTimer;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(float ticksPerSecond, long initialTimeMillis, FloatUnaryOperator targetMillisPerTick, CallbackInfo ci) {
		gs_firstUpdate = true;
	}

	@Inject(
		method = "advanceGameTime(J)I",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target =
				"Lnet/minecraft/client/DeltaTracker$Timer;deltaTicks:F"
		)
	)
	private void onModifyTickrate(long timeMillis, CallbackInfoReturnable<Integer> cir) {
		if (gs_firstUpdate) {
			init0(lastMs);
			gs_firstUpdate = false;
		}
		
		float millisPerTick = getMillisPerTick0();
		
		if (GSClientController.getInstance().isG4mespeedServer()) {
			gs_serverTimer.setMillisPerTick(millisPerTick);
		} else {
			gs_serverTimer.setMillisPerTick(DEFAULT_MILLIS_PER_TICK);
		}
		
		this.deltaTicks = (timeMillis - this.lastMs) / millisPerTick;
	}

	@Inject(
		method = "advanceGameTime(J)I",
		at = @At(
			value = "FIELD",
			shift = Shift.BEFORE,
			opcode = Opcodes.GETFIELD,
			target =
				"Lnet/minecraft/client/DeltaTracker$Timer;deltaTickResidual:F"
		)
	)
	private void onGetTicksThisFrame(long currentTimeMillis, CallbackInfoReturnable<Integer> cir) {
		gs_ticksThisFrame = (int)deltaTickResidual;
	}

	@Inject(
		method = "advanceGameTime(J)I",
		cancellable = true,
		at = @At("RETURN")
	)
	private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
		update0(timeMillis);
		cir.setReturnValue(gs_ticksThisFrame);
		cir.cancel();
	}

	@Override
	public void gs_setFromTimer(GSITickTimer timer) {
		deltaTickResidual = timer.getPartialTick0();
		pausedDeltaTickResidual = timer.getPartialTick0();
		// Note: convert to Minecraft default tps.
		realtimeDeltaTicks = timer.getLastDuration0() * timer.getMillisPerTick0() / msPerTick;
		deltaTicks = timer.getLastDuration0();
		lastUiMs = lastMs = timer.getPrevTimeMillis0();
	}
	
	/* Following methods might add compatibility issues (if other mods have same names) */

	@Override
	public void init0(long initialTimeMillis) {
		gs_tpsModule = GSClientController.getInstance().getTpsModule();
		gs_serverTimer = gs_tpsModule.getServerTimer();
		
		gs_serverTimer.init0(initialTimeMillis);
	}

	@Override
	public void update0(long timeMillis) {
		gs_serverTimer.update0(timeMillis);
		gs_serverTimer.syncTimer(this);
	}
	
	@Override
	public float getMillisPerTick0() {
		// Other mods such as the ReplayMod modify the timeScale value
		// of the timer. To ensure that the functionality stays as expected,
		// scale the milliseconds per tick by that value.
		return gs_tpsModule.getMsPerTick() * msPerTick / DEFAULT_MILLIS_PER_TICK;
	}

	@Override
	public float getPartialTick0() {
		return deltaTickResidual;
	}

	@Override
	public void setPartialTick0(float partialTick) {
		this.deltaTickResidual = partialTick;
	}

	@Override
	public int getTickCount0() {
		return gs_ticksThisFrame;
	}

	@Override
	public void setTickCount0(int tickCount) {
		this.gs_ticksThisFrame = tickCount;
	}

	@Override
	public float getLastDuration0() {
		return deltaTicks;
	}

	@Override
	public long getPrevTimeMillis0() {
		return lastMs;
	}
}
