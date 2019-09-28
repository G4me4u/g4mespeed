package com.g4mesoft.mixin.server;

import java.io.File;
import java.net.Proxy;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.server.GSControllerServer;
import com.g4mesoft.debug.GSDebug;
import com.g4mesoft.module.tps.GSITpsDependant;
import com.g4mesoft.module.tps.GSTpsModule;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.UserCache;
import net.minecraft.util.profiler.DisableableProfiler;

@Mixin(MinecraftServer.class)
public abstract class GSMinecraftServerMixin implements GSITpsDependant {

	private float msAccum = 0.0f;
	private float msPerTick = GSTpsModule.MS_PER_SEC / GSTpsModule.DEFAULT_TPS;
	private boolean tpsChanged;
	
	@Shadow @Final private static Logger LOGGER;
	@Shadow private volatile boolean running;
	@Shadow private long timeReference;
	@Shadow private long field_4557;
	@Shadow private boolean profilerStartQueued;
	@Shadow @Final private DisableableProfiler profiler;
	@Shadow private volatile boolean loading;
	@Shadow private boolean field_19249;
	@Shadow private long field_19248;

	@Shadow protected abstract void tick(BooleanSupplier booleanSupplier);

	@Shadow protected abstract boolean shouldKeepTicking();

	@Shadow protected abstract void method_16208();

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	private void onMinecraftServerCTOR(File file, Proxy proxy, DataFixer dataFixer, CommandManager commandManager,
			YggdrasilAuthenticationService authService, MinecraftSessionService sessionService,
			GameProfileRepository gameProfileRepo, UserCache userCache,
			WorldGenerationProgressListenerFactory worldGenProgressListenerFactory, String string, CallbackInfo ci) {

		GSControllerServer.getInstance().init((MinecraftServer) ((Object) this));
	}

	@Override
	public void tpsChanged(float newTps, float oldTps) {
		msPerTick = GSTpsModule.MS_PER_SEC / newTps;
		msAccum = msPerTick;
		
		tpsChanged = true;
		resetTimeReference();
	}

	private void resetTimeReference() {
		this.timeReference = this.field_19248 = SystemUtil.getMeasuringTimeMs() + (long)msPerTick;
	}
	
	@Redirect(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;running:Z"))
	private boolean disableServerLoop(MinecraftServer server) {
		return false;
	}

	@Inject(method = "run", at = @At(value = "INVOKE", shift = At.Shift.AFTER, 
			target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V"))
	private void modifiedRunLoop(CallbackInfo ci) {
		while (this.running) {
			long msThisTick = (long) msAccum;
			msAccum += msPerTick - msThisTick;

			long msBehind = SystemUtil.getMeasuringTimeMs() - this.timeReference;
			if (msBehind > 1000L + 20L * msPerTick && this.timeReference - this.field_4557 >= 10000L + 100L * msPerTick) {
				long ticksBehind = (long) (msBehind / msPerTick);
				LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", msBehind,
						ticksBehind);
				this.timeReference += ticksBehind * msPerTick;
				this.field_4557 = this.timeReference;

				this.msAccum = msPerTick;
			}

			this.timeReference += msThisTick;
			if (this.profilerStartQueued) {
				this.profilerStartQueued = false;
				this.profiler.getController().enable();
			}

			this.profiler.startTick();
			this.profiler.push("tick");
			this.tick(this::shouldKeepTicking);
			this.profiler.swap("nextTickWait");
			if (tpsChanged) {
				tpsChanged = false;
				resetTimeReference();
			} else {
				this.field_19249 = true;
				this.field_19248 = Math.max(SystemUtil.getMeasuringTimeMs() + msThisTick, this.timeReference);
			}
			this.method_16208();
			this.profiler.pop();
			this.profiler.endTick();
			this.loading = true;
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(BooleanSupplier booleanSupplier, CallbackInfo ci) {
		GSDebug.onServerTick();
		GSControllerServer.getInstance().tick();
	}
	
	@Inject(method = "shutdown", at = @At("RETURN"))
	public void onShutdown(CallbackInfo ci) {
		GSControllerServer.getInstance().onServerShutdown();
	}
}
