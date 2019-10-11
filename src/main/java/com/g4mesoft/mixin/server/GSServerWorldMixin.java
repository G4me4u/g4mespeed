package com.g4mesoft.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.g4mesoft.core.server.GSControllerServer;

import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public class GSServerWorldMixin {

	@ModifyConstant(method = "sendBlockActions", constant = @Constant(doubleValue = 64.0))
	public double blockEventDistance(double defaultDistance) {
		return GSControllerServer.getInstance().getTpsModule().sBlockEventDistance.getValue() * 16.0;
	}
}
