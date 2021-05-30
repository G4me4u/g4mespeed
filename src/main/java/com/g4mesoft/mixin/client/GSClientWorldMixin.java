package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(ClientWorld.class)
public class GSClientWorldMixin {

	@Shadow @Final private MinecraftClient client;
	
	private boolean tickingEntities;
	
	@Inject(method = "tickEntities", at = @At("HEAD"))
	public void onTickEntitiesHead(CallbackInfo ci) {
		tickingEntities = true;
	}

	@Inject(method = "tickEntities", at = @At("RETURN"))
	public void onTickEntitiesReturn(CallbackInfo ci) {
		tickingEntities = false;
	}
	
	@Inject(method = "tickEntity", cancellable = true, at = @At("HEAD"))
	public void onTickEntity(Entity entity, CallbackInfo ci) {
		if (tickingEntities && (entity instanceof AbstractClientPlayerEntity)) {
			if (GSControllerClient.getInstance().getTpsModule().isPlayerFixedMovement((AbstractClientPlayerEntity)entity))
				ci.cancel();
		}
	}
}
