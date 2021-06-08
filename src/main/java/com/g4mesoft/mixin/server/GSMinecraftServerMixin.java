package com.g4mesoft.mixin.server;

import java.util.function.BooleanSupplier;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSControllerServer;
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
		GSControllerServer controllerServer = GSControllerServer.getInstance();
		controllerServer.init((MinecraftServer)(Object)this);
		controllerServer.getTpsModule().addTpsListener(this);
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", shift = Shift.BEFORE,
			target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"))
	private void onRunServerLoopBeginning(CallbackInfo ci) {
		msThisTick = (long)msAccum;
		msAccum += msPerTick - msThisTick;
	}

	@ModifyConstant(method = "runServer", constant = @Constant(longValue = 50L))
	private long onRunServerModify50(long prevMsThisTick) {
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
		
		GSControllerServer.getInstance().tick(false);
	}
	
	@Inject(method = "shutdown", at = @At("RETURN"))
	public void onShutdown(CallbackInfo ci) {
		GSControllerServer.getInstance().onServerShutdown();
	}
}
