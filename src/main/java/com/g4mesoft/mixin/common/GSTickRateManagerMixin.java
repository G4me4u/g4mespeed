package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.GSController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.world.TickRateManager;

@Mixin(TickRateManager.class)
public class GSTickRateManagerMixin {

	@Shadow protected float tickrate;
	
	@ModifyConstant(
		method = "setTickRate",
		expect = 1,
		require = 1,
		constant = @Constant(
			floatValue = 1.0f
		)
	)
	public float setTickRateModifyMinimumTps(float oldValue) {
		return GSTpsModule.MIN_TPS;
	}
	
	@Inject(
		method = "setTickRate",
		at = @At("RETURN")
	)
	public void onSetTickRateEnd(CallbackInfo ci) {
		GSController controller = GSController.getInstanceOnThread();
		if (controller != null && controller.isClient())
			controller.getTpsModule().setTps(tickrate);
	}
}
