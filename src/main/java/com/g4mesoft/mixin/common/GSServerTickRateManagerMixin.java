package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.access.common.GSIMinecraftServerAccess;
import com.g4mesoft.access.common.GSIServerTickRateManagerAccess;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.world.TickRateManager;

@Mixin(ServerTickRateManager.class)
public abstract class GSServerTickRateManagerMixin extends TickRateManager implements GSIServerTickRateManagerAccess {

	@Shadow @Final private MinecraftServer server;

	@Shadow public abstract boolean isSprinting();
	
	@Unique private float gs_prevTickRate;
	@Unique private boolean gs_updatingTps;
	@Unique private GSTpsModule gs_tpsModule;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_prevTickRate = tickrate;
		gs_updatingTps = false;
		gs_tpsModule = GSServerController.getInstance().getTpsModule();
	}
	
	@Inject(
		method = "setTickRate",
		at = @At("RETURN")
	)
	private void onSetTickRateReturn(CallbackInfo ci) {
		try {
			gs_updatingTps = true;
			gs_tpsModule.setTps(tickrate);
		} finally {
			gs_updatingTps = false;
		}
		((GSIMinecraftServerAccess)server).gs_onTickrateChanged(tickrate, gs_prevTickRate);
		gs_prevTickRate = tickrate;
	}

	@Inject(
		method = "requestGameToSprint",
		at = @At("RETURN")
	)
	private void onRequestGameToSprint(CallbackInfoReturnable<Boolean> cir) {
		gs_tpsModule.onTickSprintChanged(isSprinting());
	}

	@Inject(
		method = "finishTickSprint",
		at = @At("RETURN")
	)
	private void onFinishTickSprint(CallbackInfo ci) {
		gs_tpsModule.onTickSprintChanged(isSprinting());
	}
	
	@Override
	public boolean gs_isUpdatingTps() {
		return gs_updatingTps;
	}
}
