package com.g4mesoft.mixin.client;

import java.util.List;

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

import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(World.class)
public abstract class GSWorldMixin implements GSIClientWorldAccess {

	@Shadow @Final private List<PlayerEntity> players;
	@Shadow @Final private boolean isClient;
	
	@Unique
	private boolean gs_tickingEntities;
	
	@Shadow public abstract void updateEntity(Entity entity);
	
	@Inject(
		method = "tickEntities",
		at = @At("HEAD")
	)
	private void onTickEntitiesHead(CallbackInfo ci) {
		gs_tickingEntities = true;
	}

	@Inject(
		method = "tickEntities",
		at = @At("RETURN")
	)
	private void onTickEntitiesReturn(CallbackInfo ci) {
		gs_tickingEntities = false;
	}
	
	@Inject(
		method = "updateEntity(Lnet/minecraft/entity/Entity;Z)V",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onUpdateEntity(Entity entity, boolean requireLoaded, CallbackInfo ci) {
		if (isClient && gs_tickingEntities && (entity instanceof ClientPlayerEntity)) {
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			if (tpsModule.isPlayerFixedMovement((ClientPlayerEntity)entity))
				ci.cancel();
		}
	}
	
	@Override
	public void gs_tickFixedMovementPlayers() {
		if (isClient) {
			GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
			for (PlayerEntity player : players) {
				if (!player.hasVehicle() && !player.removed && player instanceof ClientPlayerEntity) {
					if (tpsModule.isPlayerFixedMovement((ClientPlayerEntity)player))
						updateEntity(player);
				}
			}
		}
	}
}
