package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

@Mixin(ClientWorld.class)
public class GSClientWorldMixin {

	@Shadow @Final private MinecraftClient client;
	
	@Unique
	private boolean gs_tickingEntities;
	
	@Inject(method = "tickEntities", at = @At("HEAD"))
	private void onTickEntitiesHead(CallbackInfo ci) {
		gs_tickingEntities = true;
	}

	@Inject(method = "tickEntities", at = @At("RETURN"))
	private void onTickEntitiesReturn(CallbackInfo ci) {
		gs_tickingEntities = false;
	}
	
	@Inject(method = "tickEntity", cancellable = true, at = @At("HEAD"))
	private void onTickEntity(Entity entity, CallbackInfo ci) {
		if (gs_tickingEntities && (entity instanceof AbstractClientPlayerEntity)) {
			if (GSClientController.getInstance().getTpsModule().isPlayerFixedMovement((AbstractClientPlayerEntity)entity))
				ci.cancel();
		}
	}
}
