package com.g4mesoft.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.server.command.TickCommand;

@Mixin(TickCommand.class)
public class GSTickCommandMixin {

	@ModifyConstant(
		method = "register",
		require = 1,
		expect = 1,
		constant = @Constant(
			floatValue = 1.0f
		)
	)
	private static float onRegisterReplaceMinTickRate(float oldValue) {
		return GSTpsModule.MIN_TPS;
	}

	@ModifyConstant(
		method = "register",
		require = 1,
		expect = 1,
		constant = @Constant(
			floatValue = 10000.0f
		)
	)
	private static float onRegisterReplaceMaxTickRate(float oldValue) {
		return GSTpsModule.MAX_TPS;
	}
}
