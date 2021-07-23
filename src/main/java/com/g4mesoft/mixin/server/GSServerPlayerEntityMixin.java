package com.g4mesoft.mixin.server;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.access.GSIServerPlayerEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class GSServerPlayerEntityMixin implements GSIServerPlayerEntity {

	@Shadow public ServerPlayNetworkHandler networkHandler;
	
	private final Set<Integer> entitiesToDestroy = new HashSet<>();

	@Inject(method = "tick", at = @At("HEAD"))
	private void onTickHead(CallbackInfo ci) {
		int entityCount = entitiesToDestroy.size();
		if (entityCount != 0) {
			// Replacement for entitiesToDestroy.toArray(...)
			int[] entityIdArray = new int[entityCount];
			for (int entityId : entitiesToDestroy)
				entityIdArray[--entityCount] = entityId;
			entitiesToDestroy.clear();
			
			networkHandler.sendPacket(new EntitiesDestroyS2CPacket(entityIdArray));
		}
	}
	
	@Inject(method = "copyFrom", at = @At("RETURN"))
	private void onCopyFromReturn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		entitiesToDestroy.clear();
		entitiesToDestroy.addAll(((GSIServerPlayerEntity)oldPlayer).getEntitiesToDestroy());
	}
	
	@Override
	public void onStartTrackingFallingSand(Entity entity) {
		entitiesToDestroy.remove(entity.getId());
	}

	@Override
	public void onStopTrackingFallingSand(Entity entity) {
		entitiesToDestroy.add(entity.getId());
	}
	
	@Override
	public Set<Integer> getEntitiesToDestroy() {
		return entitiesToDestroy;
	}
}
