package com.g4mesoft.mixin.common;

import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtil;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.TickDurationMonitor;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

@Mixin(MinecraftServer.class)
public abstract class GSMinecraftServerMixin implements GSITpsDependant {

	@Shadow @Final private static Logger LOGGER;
	@Shadow private volatile boolean running;
	@Shadow private long timeReference;
	@Shadow private long lastTimeReference;
	@Shadow private boolean profilerStartQueued;
	@Shadow private Profiler profiler;
	@Shadow private volatile boolean loading;
	@Shadow private boolean waitingForNextTick;
	@Shadow private long field_19248;

	@Shadow protected abstract void tick(BooleanSupplier booleanSupplier);

	@Shadow protected abstract boolean shouldKeepTicking();

	@Shadow protected abstract void method_16208();

	@Shadow protected abstract void startMonitor(TickDurationMonitor tickDurationMonitor);
	
	@Shadow protected abstract void endMonitor(TickDurationMonitor tickDurationMonitor);

	@Unique
	private float gs_msAccum = 0.0f;
	@Unique
	private float gs_msPerTick = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;
	
	@Override
	public void tpsChanged(float newTps, float oldTps) {
		long millisPrevTick = (long)gs_msAccum;
		
		gs_msPerTick = GSTpsModule.MS_PER_SEC / newTps;
		gs_msAccum = gs_msPerTick;
		
		// We want the change in tick-rate to be as smooth as
		// possible (like on the client), however, the server
		// does not use floating points for time. Instead, we
		// have to perform, essentially, the same operation,
		// but using the reference times that the server uses.
		//
		// Let the following represent the time line:
		// <-------|------|-----|-------|------|------> t
		//        t_p2   t_p1  t_n     t_r1   t_r2
		// Where t_p1 is the time of the previous tick, t_n is
		// current time, and t_r1 is the unadjusted reference
		// time. We say that t_r2 is the reference time after
		// the adjustment, which is what we want to find, and
		// t_p2 is the time of the hypothetical previous tick
		// that would result in the reference time of t_r2.
		//
		// As on the client, we want to interpolation progress
		// to be equal before and after the adjustment:
		//   a_1 = (t_n - t_p1) / (t_r1 - t_p1) = (t_n - t_p1) / D_1,
		//   a_2 = (t_n - t_p2) / (t_r2 - t_p2) = (t_n - t_p2) / D_2,
		// where D_1 and D_2 are time per tick before and after
		// the adjustment. Therefore, we have that a_2 = a_1, and
		// can thus solve for t_p2 as follows:
		//   (t_n - t_p2) / D_2 = (t_n - t_p1) / D_1
		//   <==>
		//   t_p2 = t_n - D_2 * (t_n - t_p1) / D_1
		// Since we are not interested in t_p2, but instead want
		// to compute the adjusted reference t_r2, we can add
		// the adjusted ms per tick, and simplify as follows:
		//   t_r2 = t_n - D_2 * (t_n - t_p1) / D_1 + D_2
		//        = t_n - D_2 * ((t_n - t_p1) / D_1 - 1)
		//        = t_n - D_2 * (t_n - t_p1 - D_1) / D_1
		//        = t_n - D_2 * (t_n - t_r1) / D_1
		//        = t_n + D_2 * (t_r1 - t_n) / D_1
		// Hence, we now know how to compute the adjusted time
		// reference while preserving interpolation progress.
		// Note that there might be some inaccuracies since we
		// are using milliseconds.
		
		long now = Util.getMeasuringTimeMs();   // t_n
		long dt = timeReference - now;          // t_r1 - t_n
		long millisNextTick = (long)gs_msAccum; // D_2
		
		if (dt < millisPrevTick && millisPrevTick != 0L) {
			// t_r2 = t_n + D_2 * (t_r1 - t_n) / D_1
			long delta = millisNextTick * dt / millisPrevTick;
			timeReference = now + GSMathUtil.clamp(delta, 0L, millisNextTick);
		} else {
			timeReference = now + millisNextTick;
		}
		// Also reset wait timer for tasks.
		field_19248 = timeReference;
	}

	@Inject(
		method = "runServer",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.BEFORE, 
			target =
				"Lnet/minecraft/server/MinecraftServer;setFavicon(" +
					"Lnet/minecraft/server/ServerMetadata;" +
				")V"
		)
	)
	private void onInitialized(CallbackInfo ci) {
		// Some mods might also modify the run loop...
		// in this case just make sure that the init
		// method was called *before* those loops.
		GSServerController controllerServer = GSServerController.getInstance();
		controllerServer.init((MinecraftServer)(Object)this);
		controllerServer.getTpsModule().addTpsListener(this);
	}

	/*
	 * Ensure that we set require = 0. Some mods might change the overall structure of the mod. For 
	 * example carpet modifies the loop and changes this.running to just be false.
	 */
	@Inject(
		method = "runServer",
		require = 0,
		allow = 1,
		at = @At(
			value = "FIELD",
			shift = Shift.BEFORE,
			opcode = Opcodes.GETFIELD,
			target = "Lnet/minecraft/server/MinecraftServer;running:Z"
		)
	)
	private void onModifiedRunLoop(CallbackInfo ci) {
		while (this.running) {
			long msThisTick = (long)gs_msAccum;
			gs_msAccum += gs_msPerTick - msThisTick;

			long msBehind = Util.getMeasuringTimeMs() - this.timeReference;
			if (msBehind > 1000L + 20L * gs_msPerTick && this.timeReference - this.lastTimeReference >= 10000L + 100L * gs_msPerTick) {
				// Handle cases where msPerTick is near zero (or actually zero)
				if (GSMathUtil.equalsApproximate(gs_msPerTick, 0.0f)) {
					LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or infinite ticks behind", msBehind);
					this.timeReference += msBehind;
					this.lastTimeReference = this.timeReference;
				} else {
					long ticksBehind = (long)(msBehind / gs_msPerTick);
					LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", msBehind, ticksBehind);
					this.timeReference += ticksBehind * gs_msPerTick;
					this.lastTimeReference = this.timeReference;
				}

				this.gs_msAccum = gs_msPerTick;
			}

			this.timeReference += msThisTick;
			
			TickDurationMonitor tickDurationMonitor_1 = TickDurationMonitor.create("Server");
			this.startMonitor(tickDurationMonitor_1);
			
			this.profiler.startTick();
			this.profiler.push("tick");
			this.tick(this::shouldKeepTicking);
			this.profiler.swap("nextTickWait");
			this.waitingForNextTick = true;
			this.field_19248 = Math.max(Util.getMeasuringTimeMs() + msThisTick, this.timeReference);
			this.method_16208();
			this.profiler.pop();
			this.profiler.endTick();
			
			this.endMonitor(tickDurationMonitor_1);
			this.loading = true;
		}
	}
	
	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
		GSDebug.onServerTick();
		
		GSServerController.getInstance().tick(false);
	}
	
	@Inject(
		method = "shutdown",
		at = @At("RETURN")
	)
	private void onShutdown(CallbackInfo ci) {
		GSServerController.getInstance().onServerShutdown();
	}
}
