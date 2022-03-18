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

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.core.compat.GSCarpetCompat;
import com.g4mesoft.module.tps.GSITickTimer;
import com.g4mesoft.module.tps.GSServerTickTimer;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class GSRenderTickCounterMixin implements GSITickTimer {

	@Shadow public float tickDelta;
	@Shadow public float lastFrameDuration;
	@Shadow public long prevTimeMillis;
	@Shadow @Final private float tickTime;
	
	@Unique
	private int gs_ticksThisFrame;
	
	@Unique
	private boolean gs_firstUpdate;
	@Unique
	private GSCarpetCompat gs_carpetCompat;
	@Unique
	private GSTpsModule gs_tpsModule;
	@Unique
	private GSServerTickTimer gs_serverTimer;
	
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(float ticksPerSecond, long initialTimeMillis, CallbackInfo ci) {
		gs_firstUpdate = true;
	}

	@Inject(method = "beginRenderTick", at = @At(value = "FIELD", shift = Shift.AFTER, opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/render/RenderTickCounter;lastFrameDuration:F"))
	private void onModifyTickrate(long timeMillis, CallbackInfoReturnable<Boolean> cir) {
		if (gs_firstUpdate) {
			init0(prevTimeMillis);
			gs_firstUpdate = false;
		}
		
		float millisPerTick = getMillisPerTick0();
		
		if (GSClientController.getInstance().isG4mespeedServer()) {
			gs_serverTimer.setMillisPerTick(millisPerTick);
		} else {
			gs_serverTimer.setMillisPerTick(DEFAULT_MILLIS_PER_TICK);
		}
		
		if (!gs_carpetCompat.isTickrateLinked() || gs_tpsModule.cForceCarpetTickrate.getValue())
			this.lastFrameDuration = (timeMillis - this.prevTimeMillis) / millisPerTick;
	}

	@Inject(method = "beginRenderTick", at = @At(value = "FIELD", shift = Shift.BEFORE, opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/client/render/RenderTickCounter;tickDelta:F"))
	private void onGetTicksThisFrame(long currentTimeMillis, CallbackInfoReturnable<Integer> cir) {
		gs_ticksThisFrame = (int)tickDelta;
	}

	@Inject(method = "beginRenderTick", cancellable = true, at = @At("RETURN"))
	private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
		update0(timeMillis);
		cir.setReturnValue(gs_ticksThisFrame);
		cir.cancel();
	}

	/* Following methods might add compatibility issues (if other mods have same names) */

	@Override
	public void init0(long initialTimeMillis) {
		gs_carpetCompat = G4mespeedMod.getInstance().getCarpetCompat();
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
		return gs_tpsModule.getMsPerTick() * tickTime / DEFAULT_MILLIS_PER_TICK;
	}

	@Override
	public float getTickDelta0() {
		return tickDelta;
	}

	@Override
	public void setTickDelta0(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	@Override
	public int getTickCount0() {
		return gs_ticksThisFrame;
	}

	@Override
	public void setTickCount0(int tickCount) {
		this.gs_ticksThisFrame = tickCount;
	}
}
