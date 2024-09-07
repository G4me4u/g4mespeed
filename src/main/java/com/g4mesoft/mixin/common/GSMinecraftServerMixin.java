package com.g4mesoft.mixin.common;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIMinecraftServerAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.ui.util.GSMathUtil;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.util.Util;

@Mixin(MinecraftServer.class)
public abstract class GSMinecraftServerMixin implements GSIMinecraftServerAccess {

	@Shadow private long tickStartTimeNanos;
	@Shadow private long tickEndTimeNanos;
	
	@Shadow @Final private ServerTickManager tickManager;

	@Override
	public void gs_onTickrateChanged(float newTickrate, float oldTickrate) {
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
		
		long nsPrevTick = (long)(1.0e9d / (double)oldTickrate); // D_1
		long nsThisTick = (long)(1.0e9d / (double)newTickrate); // D_2
		
		// Check that the tick rate actually changed.
		if (nsPrevTick != nsThisTick) {
			long now = Util.getMeasuringTimeNano(); // t_n
			long dt = tickStartTimeNanos - now;     // t_r1 - t_n
			
			if (dt < nsPrevTick && nsPrevTick != 0L) {
				// t_r2 = t_n + D_2 * (t_r1 - t_n) / D_1
				long delta = nsThisTick * dt / nsPrevTick;
				tickStartTimeNanos = now + GSMathUtil.clamp(delta, 0L, nsThisTick);
			} else {
				tickStartTimeNanos = now + nsThisTick;
			}
			// Also reset wait timer for tasks.
			tickEndTimeNanos = tickStartTimeNanos;
		}
	}

	@Inject(
		method = "runServer",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE, 
			target =
				"Lnet/minecraft/server/MinecraftServer;createMetadata(" +
				")Lnet/minecraft/server/ServerMetadata;"
		)
	)
	private void onInitialized(CallbackInfo ci) {
		// Some mods might also modify the run loop...
		// in this case just make sure that the init
		// method was called *before* those loops.
		GSServerController controllerServer = GSServerController.getInstance();
		controllerServer.init((MinecraftServer)(Object)this);
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
