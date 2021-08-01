package com.g4mesoft.mixin.server;

import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

	private float msAccum = 0.0f;
	private float msPerTick = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;
	
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

	@Override
	public void tpsChanged(float newTps, float oldTps) {
		long millisPrevTick = (long)msAccum;
		
		msPerTick = GSTpsModule.MS_PER_SEC / newTps;
		msAccum = msPerTick;
		
		long now = Util.getMeasuringTimeMs();
		long dt = timeReference - now;
		long millisNextTick = (long)msAccum;
		
		if (dt < millisPrevTick && millisPrevTick != 0L) {
			// Interpolate the progress until next tick with the
			// new milliseconds per tick. This ensures that the
			// next tick will be very close to the client tick.
			long delta = dt * millisNextTick / millisPrevTick;
			timeReference = now + GSMathUtil.clamp(delta, 0L, millisNextTick);
		} else {
			timeReference = now + millisNextTick;
		}
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, 
			target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V"))
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
	@Inject(method = "runServer", require = 0, allow = 1, at = @At(value = "FIELD", shift = Shift.BEFORE,
			opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/MinecraftServer;running:Z"))
	private void onModifiedRunLoop(CallbackInfo ci) {
		while (this.running) {
			long msThisTick = (long)msAccum;
			msAccum += msPerTick - msThisTick;

			long msBehind = Util.getMeasuringTimeMs() - this.timeReference;
			if (msBehind > 1000L + 20L * msPerTick && this.timeReference - this.lastTimeReference >= 10000L + 100L * msPerTick) {
				// Handle cases where msPerTick is near zero (or actually zero)
				if (GSMathUtil.equalsApproximate(msPerTick, 0.0f)) {
					LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or infinite ticks behind", msBehind);
					this.timeReference += msBehind;
					this.lastTimeReference = this.timeReference;
				} else {
					long ticksBehind = (long)(msBehind / msPerTick);
					LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", msBehind, ticksBehind);
					this.timeReference += ticksBehind * msPerTick;
					this.lastTimeReference = this.timeReference;
				}

				this.msAccum = msPerTick;
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
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
		GSDebug.onServerTick();
		
		GSServerController.getInstance().tick(false);
	}
	
	@Inject(method = "shutdown", at = @At("RETURN"))
	public void onShutdown(CallbackInfo ci) {
		GSServerController.getInstance().onServerShutdown();
	}
}
