package com.g4mesoft.mixin.common;

import java.util.Queue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.ui.util.GSMathUtil;
import com.google.common.util.concurrent.ListenableFuture;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.handler.CommandHandler;
import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.util.Utils;

@Mixin(MinecraftServer.class)
public abstract class GSMinecraftServerMixin implements GSITpsDependant {

	@Shadow @Final private static Logger LOGGER;
	@Shadow private long nextTickTime; /* prevTickTime */
	@Shadow @Final public Queue<FutureTask<?>> pendingEvents;
	@Shadow private Thread thread;
	@Shadow @Final public CommandHandler commandHandler;
	
	@Unique
	private float gs_msAccum = 0.0f;
	@Unique
	private float gs_msPerTick = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;

	@Unique
	private long gs_msThisTick;
	@Unique
	private long gs_ticksBehind;
	
	@Unique
	private long gs_nextTickTime;
	
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
		
		long now = MinecraftServer.getTimeMillis(); // t_n
		long dt = gs_nextTickTime - now;            // t_r1 - t_n
		long millisNextTick = (long)gs_msAccum;     // D_2
		
		if (dt < millisPrevTick && millisPrevTick != 0L) {
			// t_r2 = t_n + D_2 * (t_r1 - t_n) / D_1
			long delta = millisNextTick * dt / millisPrevTick;
			gs_nextTickTime = now + GSMathUtil.clamp(delta, 0L, millisNextTick);
		} else {
			gs_nextTickTime = now + millisNextTick;
		}
		
		nextTickTime /* prevTickTime */ = gs_nextTickTime - millisNextTick;
	}

	@Inject(
		method = "run",
		at = @At(
			value = "INVOKE",
			shift = At.Shift.BEFORE, 
			target =
				"Lnet/minecraft/server/MinecraftServer;setStatus(" +
					"Lnet/minecraft/server/ServerStatus;" +
				")V"
		)
	)
	private void onInitialized(CallbackInfo ci) {
		// Some mods might also modify the run loop...
		// in this case just make sure that the init
		// method was called *before* those loops.
		GSServerController controllerServer = GSServerController.getInstance();
		controllerServer.init((MinecraftServer)(Object)this);
		controllerServer.setCommandManager((CommandManager)commandHandler);
		controllerServer.getTpsModule().addTpsListener(this);
		
		// Initial tick is happening now.
		gs_nextTickTime = nextTickTime /* prevTickTime */;
	}
	
	@ModifyVariable(
		method = "run",
		expect = 1,
		ordinal = 0,
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				shift = At.Shift.AFTER, 
				target =
					"Lnet/minecraft/server/MinecraftServer;setStatus(" +
						"Lnet/minecraft/server/ServerStatus;" +
					")V"
			)
		),
		at = @At(
			value = "FIELD",
			shift = Shift.BEFORE,
			opcode = Opcodes.PUTFIELD,
			target =
				"Lnet/minecraft/server/MinecraftServer;nextTickTime:J"
		)
	)
	private long onRunServerLoopBeginning(long prevTimeRemaining) {
		gs_msThisTick = (long)gs_msAccum;
		gs_msAccum += gs_msPerTick - gs_msThisTick;
		// Prepare next tick time.
		gs_nextTickTime += gs_msThisTick;
		// Note: anything greater than 0 would do, as we replace while loop with
		//       a while(prevTimeRemaining > 0L) { ... }.
		return 1L;
	}

	@ModifyConstant(
		method = "run",
		constant = @Constant(
			longValue = 50L,
			ordinal = 0
		)
	)
	private long onRunServerModify50_0(long prevMsThisTick) {
		if (GSMathUtil.equalsApproximate(gs_msPerTick, 0.0f)) {
			gs_ticksBehind = Long.MAX_VALUE;
		} else {
			long deltaMs = MinecraftServer.getTimeMillis() - nextTickTime;
			gs_ticksBehind = (deltaMs > 0L) ? (long)(deltaMs / gs_msPerTick) : 0L;
		}
		
		/* Does not matter what is returned here as long as it is non-zero */
		return 1L;
	}

	@ModifyArg(
		method = "run",
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
		method = "run",
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
	private void onRunServerAfterCantKeepUpWarn(CallbackInfo ci) {
		nextTickTime /* prevTickTime */ = MinecraftServer.getTimeMillis();
		if (gs_ticksBehind == Long.MAX_VALUE) {
			gs_nextTickTime = nextTickTime;
		} else {
			gs_nextTickTime += gs_ticksBehind * gs_msPerTick;
		}
	}

	@Inject(
		method = "run",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lorg/apache/logging/log4j/Logger;warn(" +
					"Ljava/lang/String;" +
				")V"
		)
	)
	private void onRunServerAfterRunBackwardsWarn(CallbackInfo ci) {
		nextTickTime /* prevTickTime */ = gs_nextTickTime = MinecraftServer.getTimeMillis();
	}

	@ModifyConstant(
		method = "run",
		constant = @Constant(
			longValue = 50L,
			ordinal = 1
		)
	)
	private long onRunServerModify50_1(long prevMsThisTick) {
		// Replace with while(... > 0L) { ...}.
		return 0L;
	}
	
	@ModifyConstant(
		method = "run",
		constant = @Constant(
			longValue = 50L,
			ordinal = 2
		)
	)
	private long onRunServerModify50_2(long prevMsThisTick) {
		// Note: should match modification of variable above.
		return 1L;
	}

	@ModifyConstant(
		method = "run",
		constant = @Constant(
			longValue = 2000L
		)
	)
	private long onRunServerModify2000(long prevMsThisTick) {
		return (long)(1000L + 20L * gs_msPerTick);
	}

	@ModifyConstant(
		method = "run",
		constant = @Constant(
			longValue = 15000L
		)
	)
	private long onRunServerModify15000(long prevMsThisTick) {
		return (long)(10000L + 100L * gs_msPerTick);
	}
	
	@Inject(
		method = "run",
		at = @At(
			value = "FIELD",
			shift = Shift.AFTER,
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/server/MinecraftServer;lastWarnTime:J"
		)
	)
	private void onRunServerAfterOverloaded(CallbackInfo ci) {
		this.gs_msAccum = gs_msPerTick;
	}

	@Unique
	private boolean gs_hasTimeLeft() {
		return gs_nextTickTime - MinecraftServer.getTimeMillis() > 0L;
	}
	
	@Redirect(
		method = "run",
		expect = 1,
		at = @At(
			value = "INVOKE",
			target =
				"Ljava/lang/Thread;sleep(" +
					"J" +
				")V"
		)
	)
	private void onRunServerRedirectThreadSleep(long ignore) {
		while (gs_hasTimeLeft()) {
			// Note: tick already ran tasks that were available at the time, so we
			//       should just execute/wait for tasks here.
			FutureTask<?> task;
			synchronized (pendingEvents) {
				task = pendingEvents.poll();
			}
			if (task == null) {
				// A yield here could avoid the need to park the thread leading to
				// quicker responses to incoming tasks.
				Thread.yield();
				// Wait for up to 100 microseconds.
				LockSupport.parkNanos("waiting for tasks", 100000L);
			} else {
				// Execute the task.
				Utils.run(task, LOGGER);
			}
		}
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci) {
		GSDebug.onServerTick();
		
		GSServerController.getInstance().tick(false);
	}
	
	@Inject(
		method =
			"submit(" +
				"Ljava/util/concurrent/Callable;" +
			")Lcom/google/common/util/concurrent/ListenableFuture;",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Ljava/util/Queue;add(" +
					"Ljava/lang/Object;" +
				")Z"
		)
	)
	private void onSubmitAfterPendingEventsAdd(CallbackInfoReturnable<ListenableFuture<?>> cir) {
		// Signal server thread (in case it is) waiting for tasks.
		LockSupport.unpark(thread);
	}

	@Inject(
		method = "stop",
		at = @At("RETURN")
	)
	private void onShutdown(CallbackInfo ci) {
		GSServerController.getInstance().onServerShutdown();
	}
}
