package com.g4mesoft.mixin.common;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.common.GSIServerPlayerEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class GSServerPlayerEntityMixin implements GSIServerPlayerEntity {

	@Shadow public ServerPlayNetworkHandler networkHandler;
	
	@Unique
	private final Set<Integer> gs_entitiesToDestroy = new HashSet<>();

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTickHead(CallbackInfo ci) {
		int entityCount = gs_entitiesToDestroy.size();
		if (entityCount != 0) {
			// Replacement for entitiesToDestroy.toArray(...)
			int[] entityIdArray = new int[entityCount];
			for (int entityId : gs_entitiesToDestroy)
				entityIdArray[--entityCount] = entityId;
			gs_entitiesToDestroy.clear();
			
			networkHandler.sendPacket(new EntitiesDestroyS2CPacket(entityIdArray));
		}
	}
	
	@Inject(
		method = "copyFrom",
		at = @At("RETURN")
	)
	private void onCopyFromReturn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		gs_entitiesToDestroy.clear();
		gs_entitiesToDestroy.addAll(((GSIServerPlayerEntity)oldPlayer).gs_getEntitiesToDestroy());
	}
	
	@Override
	public void gs_onStartTrackingFallingSand(Entity entity) {
		gs_entitiesToDestroy.remove(entity.getId());
	}

	@Override
	public void gs_onStopTrackingFallingSand(Entity entity) {
		gs_entitiesToDestroy.add(entity.getId());
	}
	
	@Override
	public Set<Integer> gs_getEntitiesToDestroy() {
		return gs_entitiesToDestroy;
	}
}
