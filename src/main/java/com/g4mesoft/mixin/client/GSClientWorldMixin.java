package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.client.GSIClientWorldAccess;
import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.EntityList;
import net.minecraft.world.World;

@Mixin(ClientWorld.class)
public abstract class GSClientWorldMixin implements GSIClientWorldAccess {

	@Shadow @Final private MinecraftClient client;
	@Shadow @Final EntityList entityList;

	@Unique
	private boolean gs_tickingEntities;
	@Unique
	private GSTpsModule gs_tpsModule = GSClientController.getInstance().getTpsModule();

	@Shadow public abstract void tickEntity(Entity entity);
	
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
			if (gs_tpsModule.isPlayerFixedMovement((AbstractClientPlayerEntity)entity))
				ci.cancel();
		}
	}
	
	@Override
	public void gs_tickFixedMovementPlayers() {
		entityList.forEach((entity) -> {
			if (entity instanceof AbstractClientPlayerEntity) {
				AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entity;
				if (!player.hasVehicle() && !player.isRemoved() && gs_tpsModule.isPlayerFixedMovement(player))
					((World)(Object)this).tickEntity(this::tickEntity, player);
			}
		});
	}
}
