package com.g4mesoft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.compat.GSICarpetTickrateManager;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSServerTickTimer;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.TickTimer;

@Mixin(TickTimer.class)
public class GSTickTimerMixin implements GSITickTimer {

	@Shadow float tps;
	@Shadow public int ticksThisFrame;
	@Shadow public float partialTick;
	@Shadow public float tickDelta;
	@Shadow public long lastTickTime;
	@Shadow public float tpsScale;
	@Shadow private double tickTimeCorrection;
	
	@Unique
	private boolean gs_firstUpdate;
	@Unique
	private long gs_currentTimeMillis;
	@Unique
	private float gs_prevTickDelta;
	@Unique
	private GSTpsModule gs_tpsModule;
	@Unique
	private GSICarpetTickrateManager gs_carpetTickrateManager;
	@Unique
	private GSServerTickTimer gs_serverTimer;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_firstUpdate = true;
	}

	@Inject(
		method = "advance",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "INVOKE_ASSIGN",
			target =
				"Lnet/minecraft/client/Minecraft;getTime(" +
				")J"
		)
	)
	private void onAdvanceAfterGetTime(CallbackInfo ci, long timeMillis) {
		if (gs_firstUpdate) {
			init(lastTickTime);
			gs_firstUpdate = false;
		}
		
		gs_currentTimeMillis = timeMillis;
		gs_prevTickDelta = tickDelta;
	}
	
	@Inject(
		method = "advance",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/TickTimer;tickTimeCorrection:D"
		)
	)
	private void onAdvanceRemoveTickCorrection(CallbackInfo ci) {
		// Since 1.11 and below, there is an extra tick correction term
		// which only makes matters worse for the first few seconds, and
		// has no effect after a while. Disable it here.
		tickTimeCorrection = 1.0;
	}
	
	@Inject(
		method = "advance",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(
			value = "FIELD",
			ordinal = 0,
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/TickTimer;tickDelta:F"
		)
	)
	private void onAdvanceModifyTickrate(CallbackInfo ci, long timeMillis, long deltaMillis, long timeMillis2, double timeSeconds, double deltaSeconds) {
		float millisPerTick = getMillisPerTick();
		
		if (GSClientController.getInstance().isG4mespeedServer()) {
			gs_serverTimer.setMillisPerTick(millisPerTick);
		} else {
			gs_serverTimer.setMillisPerTick(DEFAULT_MILLIS_PER_TICK);
		}
		
		if (!gs_carpetTickrateManager.isTickrateLinked() || gs_tpsModule.cForceCarpetTickrate.get())
			tickDelta = (float)(gs_prevTickDelta + deltaSeconds * 1000.0 / millisPerTick);
	}

	@Inject(
		method = "advance",
		at = @At("RETURN")
	)
	private void onAdvanceReturn(CallbackInfo ci) {
		update(gs_currentTimeMillis);
	}

	/* Following methods might add compatibility issues (if other mods have same names) */

	@Override
	public void init(long initialTimeMillis) {
		gs_tpsModule = GSClientController.getInstance().getTpsModule();
		gs_carpetTickrateManager = G4mespeedMod.getCarpetCompat().getClientTickrateManager();
		gs_serverTimer = gs_tpsModule.getServerTimer();
		
		gs_serverTimer.init(initialTimeMillis);
	}

	@Override
	public void update(long timeMillis) {
		gs_serverTimer.update(timeMillis);
		gs_serverTimer.syncTimer(this);
	}
	
	@Override
	public float getMillisPerTick() {
		// Other mods such as the ReplayMod modify the timeScale value
		// of the timer. To ensure that the functionality stays as expected,
		// scale the milliseconds per tick by that value.
		return gs_tpsModule.getMsPerTick() * DEFAULT_TICKS_PER_SECOND / Math.max(tps * tpsScale, 1e-5f);
	}

	@Override
	public float getTickDelta0() {
		return tickDelta;
	}

	@Override
	public void setTickDelta0(float tickDelta) {
		this.tickDelta = tickDelta;
		this.partialTick = tickDelta;
	}

	@Override
	public int getTickCount() {
		return ticksThisFrame;
	}

	@Override
	public void setTickCount(int tickCount) {
		this.ticksThisFrame = Math.min(tickCount, 10);
	}
}
