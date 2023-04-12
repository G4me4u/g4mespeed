package com.g4mesoft.mixin.common;

import java.util.function.BooleanSupplier;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
import com.g4mesoft.ui.util.GSMathUtil;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

@Mixin(MinecraftServer.class)
public abstract class GSMinecraftServerMixin implements GSITpsDependant {

	@Shadow private long timeReference;
	@Shadow private long nextTickTimestamp;

	@Unique
	private float gs_msAccum = 0.0f;
	@Unique
	private float gs_msPerTick = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;

	@Unique
	private long gs_msThisTick;
	@Unique
	private long gs_ticksBehind;
	
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
		nextTickTimestamp = timeReference;
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

	@Inject(
		method = "runServer",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				shift = At.Shift.AFTER, 
				target =
					"Lnet/minecraft/server/MinecraftServer;setFavicon(" +
						"Lnet/minecraft/server/ServerMetadata;" +
					")V"
			)
		),
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			ordinal = 0,
			target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"
		)
	)
	private void onRunServerLoopBeginning(CallbackInfo ci) {
		gs_msThisTick = (long)gs_msAccum;
		gs_msAccum += gs_msPerTick - gs_msThisTick;
	}

	@ModifyConstant(
		method = "runServer",
		constant = @Constant(
			longValue = 50L,
			ordinal = 0
		)
	)
	private long onRunServerModify50_0(long prevMsThisTick) {
		if (GSMathUtil.equalsApproximate(gs_msPerTick, 0.0f)) {
			gs_ticksBehind = Long.MAX_VALUE;
		} else {
			long deltaMs = Util.getMeasuringTimeMs() - timeReference;
			gs_ticksBehind = (deltaMs > 0L) ? (long)(deltaMs / gs_msPerTick) : 0L;
		}
		
		/* Does not matter what is returned here as long as it is non-zero */
		return 1L;
	}

	@ModifyArg(
		method = "runServer",
		require = 0,
		index = 2,
		at = @At(
			value = "INVOKE",
			target =
				"Lorg/apache/logging/log4j/Logger;warn(" +
					"Ljava/lang/String;" +
					"Ljava/lang/Object;" +
					"Ljava/lang/Object;" +
				")V"
		)
	)
	private Object modifyRunServerWarnTicksBehind(Object ignore) {
		// Modify debug message to account for "infinite" ticks per second
		return (gs_ticksBehind == Long.MAX_VALUE) ? "infinite" : Long.valueOf(gs_ticksBehind);
	}
	
	@Inject(
		method = "runServer",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lorg/apache/logging/log4j/Logger;warn(" +
					"Ljava/lang/String;" +
					"Ljava/lang/Object;" +
					"Ljava/lang/Object;" +
				")V"
		)
	)
	private void onRunServerAfterWarn(CallbackInfo ci) {
		if (gs_ticksBehind == Long.MAX_VALUE) {
			timeReference = Util.getMeasuringTimeMs();
		} else {
			timeReference += gs_ticksBehind * gs_msPerTick;
		}
	}

	@ModifyConstant(
		method = "runServer",
		constant = @Constant(
			longValue = 50L,
			ordinal = 1
		)
	)
	private long onRunServerModify50_1(long prevMsThisTick) {
		// Modifying this constant to zero will ensure that no time is added to timeReference.
		return 0L;
	}
	
	@ModifyConstant(
		method = "runServer",
		constant = @Constant(
			longValue = 50L
		),
		slice = @Slice(
			from = @At(
				value = "FIELD",
				shift = Shift.AFTER,
				opcode = Opcodes.GETFIELD,
				target = "Lnet/minecraft/server/MinecraftServer;needsDebugSetup:Z"
			)
		)
	)
	private long onRunServerModify50AfterDebugSetup(long prevMsThisTick) {
		return gs_msThisTick;
	}
	
	@ModifyConstant(
		method = "runServer",
		constant = @Constant(
			longValue = 2000L
		)
	)
	private long onRunServerModify2000(long prevMsThisTick) {
		return (long)(1000L + 20L * gs_msPerTick);
	}

	@ModifyConstant(
		method = "runServer",
		constant = @Constant(
			longValue = 15000L
		)
	)
	private long onRunServerModify15000(long prevMsThisTick) {
		return (long)(10000L + 100L * gs_msPerTick);
	}
	
	@Inject(
		method = "runServer",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/server/MinecraftServer;lastTimeReference:J"
		)
	)
	private void onRunServerAfterOverloaded(CallbackInfo ci) {
		this.gs_msAccum = gs_msPerTick;
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
