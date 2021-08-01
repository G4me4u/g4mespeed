package com.g4mesoft.mixin.server;

import java.util.function.BooleanSupplier;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.util.GSMathUtil;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

@Mixin(MinecraftServer.class)
public abstract class GSMinecraftServerMixin implements GSITpsDependant {

	private float msAccum = 0.0f;
	private float msPerTick = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;

	private long msThisTick;
	private long ticksBehind;
	
	@Shadow private long timeReference;
	
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

	@Inject(method = "runServer", at = @At(value = "INVOKE", shift = Shift.BEFORE,
			target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"))
	private void onRunServerLoopBeginning(CallbackInfo ci) {
		msThisTick = (long)msAccum;
		msAccum += msPerTick - msThisTick;
	}

	@ModifyConstant(method = "runServer", constant = @Constant(longValue = 50L, ordinal = 0))
	private long onRunServerModify50_0(long prevMsThisTick) {
		if (GSMathUtil.equalsApproximate(msPerTick, 0.0f)) {
			ticksBehind = Long.MAX_VALUE;
		} else {
			long deltaMs = Util.getMeasuringTimeMs() - timeReference;
			ticksBehind = (deltaMs > 0L) ? (long)(deltaMs / msPerTick) : 0L;
		}
		
		/* Does not matter what is returned here as long as it is non-zero */
		return 1L;
	}

	@ModifyArg(method = "runServer", require = 0, index = 2, at = @At(value = "INVOKE",
			target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
	private Object modifyRunServerWarnTicksBehind(Object ignore) {
		// Modify debug message to account for "infinite" ticks per second
		return (ticksBehind == Long.MAX_VALUE) ? "infinite" : Long.valueOf(ticksBehind);
	}
	
	@Inject(method = "runServer", at = @At(value = "INVOKE", shift = Shift.AFTER,
	        target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
	private void onRunServerAfterWarn(CallbackInfo ci) {
		if (ticksBehind == Long.MAX_VALUE) {
			timeReference = Util.getMeasuringTimeMs();
		} else {
			timeReference += ticksBehind * msPerTick;
		}
	}

	@ModifyConstant(method = "runServer", constant = @Constant(longValue = 50L, ordinal = 1))
	private long onRunServerModify50_1(long prevMsThisTick) {
		// Modifying this constant to zero will ensure that no time is added to timeReference.
		return 0L;
	}
	
	@ModifyConstant(method = "runServer", constant = @Constant(longValue = 50L), slice = @Slice(from = @At(value = "FIELD",
			shift = Shift.AFTER, opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/MinecraftServer;needsDebugSetup:Z")))
	private long onRunServerModify50DebugSetup(long prevMsThisTick) {
		return msThisTick;
	}
	
	@ModifyConstant(method = "runServer", constant = @Constant(longValue = 2000L))
	private long onRunServerModify2000(long prevMsThisTick) {
		return (long)(1000L + 20L * msPerTick);
	}

	@ModifyConstant(method = "runServer", constant = @Constant(longValue = 15000L))
	private long onRunServerModify15000(long prevMsThisTick) {
		return (long)(10000L + 100L * msPerTick);
	}
	
	@Inject(method = "runServer", at = @At(value = "FIELD", target="Lnet/minecraft/server/MinecraftServer;lastTimeReference:J", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void onRunServerAfterOverloaded(CallbackInfo ci) {
		this.msAccum = msPerTick;
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
